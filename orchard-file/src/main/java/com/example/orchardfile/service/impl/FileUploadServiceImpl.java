package com.example.orchardfile.service.impl;

import com.example.orchardcommon.business.SnowflakeId.BizCodeEnum;
import com.example.orchardcommon.business.SnowflakeId.SnowflakeUtils;
import com.example.orchardcommon.exception.BizException;
import com.example.orchardfile.config.CosConfig;
import com.example.orchardfile.dto.FileUnifiedUploadDto;
import com.example.orchardfile.dto.FileUploadBase64Dto;
import com.example.orchardfile.dto.FileUploadUrlDto;
import com.example.orchardfile.entity.FileRecord;
import com.example.orchardfile.mapper.FileRecordMapper;
import com.example.orchardfile.service.FileUploadService;
import com.example.orchardfile.vo.FileUploadVo;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.UploadPartRequest;
import com.qcloud.cos.model.UploadPartResult;
import com.qcloud.cos.model.InitiateMultipartUploadRequest;
import com.qcloud.cos.model.InitiateMultipartUploadResult;
import com.qcloud.cos.model.CompleteMultipartUploadRequest;
import com.qcloud.cos.model.PartETag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

/**
 * 文件上传服务实现
 *
 * 判断规则：folderId != null（含 0=根目录）→ 写 file_record 并绑定该目录；
 *          folderId == null → 只上传到 COS，返回 fileUrl，不写数据库。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileUploadServiceImpl implements FileUploadService {

    private final COSClient cosClient;
    private final CosConfig cosConfig;
    private final FileRecordMapper fileRecordMapper;

    // ============== 二进制文件上传 ==============

    @Override
    public FileUploadVo uploadFile(MultipartFile file, Long userId, Long folderId) {
        try {
            validateFileSize(file.getSize());

            if (file.getSize() > cosConfig.getMultipartThreshold()) {
                return uploadMultipartFile(file, userId, folderId);
            }

            CosUploadResult cosResult = doUploadToCos(file, userId);
            Long recordId = (folderId != null) ? saveFileRecord(cosResult, userId, folderId, file.getContentType()).getId() : null;

            FileUploadVo vo = toFileUploadVo(cosResult, recordId);
            log.info("文件上传：folderId={}, userId={}, fileName={}, size={}, writeDb={}", folderId, userId, cosResult.getFileName(), cosResult.getFileSize(), folderId != null);
            return vo;

        } catch (Exception e) {
            log.error("文件上传失败：folderId={}, userId={}, error={}", folderId, userId, e.getMessage(), e);
            throw new BizException("文件上传失败：" + e.getMessage());
        }
    }

    @Override
    public List<FileUploadVo> uploadFileBatch(List<MultipartFile> files, Long userId, Long folderId) {
        List<FileUploadVo> results = new ArrayList<>();
        for (MultipartFile file : files) {
            FileUploadVo vo = uploadFile(file, userId, folderId);
            results.add(vo);
        }
        log.info("批量文件上传：folderId={}, userId={}, count={}, writeDb={}", folderId, userId, files.size(), folderId != null);
        return results;
    }

    @Override
    public FileUploadVo uploadMultipartFile(MultipartFile file, Long userId, Long folderId) {
        try {
            validateFileSize(file.getSize());

            String originalName = file.getOriginalFilename();
            String fileType = getFileType(originalName);
            String fileName = generateFileName(originalName);
            String cosPath = generateCosPath(userId, fileName);

            InitiateMultipartUploadRequest initRequest = new InitiateMultipartUploadRequest(cosConfig.getBucket(), cosPath);
            InitiateMultipartUploadResult initResult = cosClient.initiateMultipartUpload(initRequest);
            String uploadId = initResult.getUploadId();

            long fileSize = file.getSize();
            long partSize = cosConfig.getPartSize();
            int partCount = (int) (fileSize / partSize);
            if (fileSize % partSize != 0) {
                partCount++;
            }

            List<PartETag> partETags = new ArrayList<>();
            byte[] fileBytes = file.getBytes();

            for (int i = 0; i < partCount; i++) {
                long startPos = i * partSize;
                long curPartSize = Math.min(partSize, fileSize - startPos);

                InputStream partStream = new ByteArrayInputStream(fileBytes, (int) startPos, (int) curPartSize);

                UploadPartRequest uploadPartRequest = new UploadPartRequest();
                uploadPartRequest.setBucketName(cosConfig.getBucket());
                uploadPartRequest.setKey(cosPath);
                uploadPartRequest.setUploadId(uploadId);
                uploadPartRequest.setInputStream(partStream);
                uploadPartRequest.setPartSize(curPartSize);
                uploadPartRequest.setPartNumber(i + 1);

                UploadPartResult uploadPartResult = cosClient.uploadPart(uploadPartRequest);
                partETags.add(uploadPartResult.getPartETag());

                log.info("分块上传进度：userId={}, part={}/{}", userId, i + 1, partCount);
            }

            CompleteMultipartUploadRequest completeRequest = new CompleteMultipartUploadRequest(
                    cosConfig.getBucket(), cosPath, uploadId, partETags
            );
            cosClient.completeMultipartUpload(completeRequest);

            String fileUrl = generateFileUrl(cosPath);

            CosUploadResult cosResult = new CosUploadResult();
            cosResult.setFileName(fileName);
            cosResult.setOriginalName(originalName);
            cosResult.setFileSize(fileSize);
            cosResult.setFileUrl(fileUrl);
            cosResult.setFileType(fileType);
            cosResult.setCosPath(cosPath);

            Long recordId = null;
            if (folderId != null) {
                FileRecord record = new FileRecord();
                record.setId(SnowflakeUtils.nextId(BizCodeEnum.FILE));
                record.setUserId(userId);
                record.setFileName(fileName);
                record.setOriginalName(originalName);
                record.setFileType(fileType);
                record.setMimeType(file.getContentType());
                record.setFileSize(fileSize);
                record.setCosPath(cosPath);
                record.setFileUrl(fileUrl);
                // folderId = 0 视为根目录，存数据库用 null
                Long realFolderId = (folderId == 0L) ? null : folderId;
                record.setFolderId(realFolderId);
                record.setStatus(1);
                record.setCreateTime(java.time.LocalDateTime.now());
                fileRecordMapper.insert(record);
                recordId = record.getId();
            }

            FileUploadVo vo = toFileUploadVo(cosResult, recordId);

            log.info("大文件分块上传：folderId={}, userId={}, fileName={}, size={}, writeDb={}", folderId, userId, fileName, fileSize, folderId != null);
            return vo;

        } catch (Exception e) {
            log.error("大文件分块上传失败：folderId={}, userId={}, error={}", folderId, userId, e.getMessage(), e);
            throw new BizException("大文件上传失败：" + e.getMessage());
        }
    }

    // ============== Base64上传 ==============

    @Override
    public FileUploadVo uploadFileBase64(String base64, String fileName, Long userId, Long folderId) {
        try {
            CosUploadResult cosResult = doUploadBase64ToCos(base64, fileName, userId);
            Long recordId = null;
            if (folderId != null) {
                String mimeType = getMimeType(cosResult.getFileType());
                recordId = saveFileRecord(cosResult, userId, folderId, mimeType).getId();
            }

            FileUploadVo vo = toFileUploadVo(cosResult, recordId);
            log.info("Base64上传：folderId={}, userId={}, fileName={}, size={}, writeDb={}", folderId, userId, cosResult.getFileName(), cosResult.getFileSize(), folderId != null);
            return vo;

        } catch (Exception e) {
            log.error("Base64上传失败：folderId={}, userId={}, error={}", folderId, userId, e.getMessage(), e);
            throw new BizException("Base64文件上传失败：" + e.getMessage());
        }
    }

    @Override
    public List<FileUploadVo> uploadFileBase64Batch(List<FileUploadBase64Dto> fileList, Long defaultUserId, Long defaultFolderId) {
        List<FileUploadVo> results = new ArrayList<>();
        for (FileUploadBase64Dto dto : fileList) {
            Long fileUserId = dto.getUserId() != null ? dto.getUserId() : defaultUserId;
            Long fileFolderId = dto.getFolderId() != null ? dto.getFolderId() : defaultFolderId;
            FileUploadVo vo = uploadFileBase64(dto.getBase64(), dto.getFileName(), fileUserId, fileFolderId);
            results.add(vo);
        }
        log.info("批量Base64上传：userId={}, count={}", defaultUserId, fileList.size());
        return results;
    }

    // ============== URL上传 ==============

    @Override
    public FileUploadVo uploadFileByUrl(String url, String fileName, Long userId, Long folderId) {
        try {
            CosUploadResult cosResult = doUploadUrlToCos(url, fileName, userId);
            Long recordId = null;
            if (folderId != null) {
                String mimeType = getMimeType(cosResult.getFileType());
                recordId = saveFileRecord(cosResult, userId, folderId, mimeType).getId();
            }

            FileUploadVo vo = toFileUploadVo(cosResult, recordId);
            log.info("URL上传：folderId={}, userId={}, url={}, fileName={}, writeDb={}", folderId, userId, url, cosResult.getFileName(), folderId != null);
            return vo;

        } catch (Exception e) {
            log.error("URL上传失败：folderId={}, userId={}, url={}, error={}", folderId, userId, url, e.getMessage(), e);
            throw new BizException("URL文件上传失败：" + e.getMessage());
        }
    }

    @Override
    public List<FileUploadVo> uploadFileByUrlBatch(List<FileUploadUrlDto> fileList, Long defaultUserId, Long defaultFolderId) {
        List<FileUploadVo> results = new ArrayList<>();
        for (FileUploadUrlDto dto : fileList) {
            Long fileUserId = dto.getUserId() != null ? dto.getUserId() : defaultUserId;
            Long fileFolderId = dto.getFolderId() != null ? dto.getFolderId() : defaultFolderId;
            FileUploadVo vo = uploadFileByUrl(dto.getUrl(), dto.getFileName(), fileUserId, fileFolderId);
            results.add(vo);
        }
        log.info("批量URL上传：userId={}, count={}", defaultUserId, fileList.size());
        return results;
    }

    // ============== 统一上传入口 ==============

    @Override
    public List<FileUploadVo> unifiedUpload(FileUnifiedUploadDto dto, Long userId) {
        Long folderId = dto.getFolderId();
        Long finalUserId = dto.getUserId() != null ? dto.getUserId() : userId;

        List<FileUploadVo> results;

        if (dto.getBase64List() != null && !dto.getBase64List().isEmpty()) {
            results = uploadFileBase64Batch(dto.getBase64List(), finalUserId, folderId);
        } else if (dto.getBase64() != null && !dto.getBase64().isEmpty()) {
            results = List.of(uploadFileBase64(dto.getBase64(), dto.getFileName(), finalUserId, folderId));
        } else if (dto.getUrlList() != null && !dto.getUrlList().isEmpty()) {
            results = uploadFileByUrlBatch(dto.getUrlList(), finalUserId, folderId);
        } else if (dto.getUrl() != null && !dto.getUrl().isEmpty()) {
            results = List.of(uploadFileByUrl(dto.getUrl(), dto.getFileName(), finalUserId, folderId));
        } else {
            throw new BizException("上传内容不能为空（base64/base64List/url/urlList 至少传一项），二进制文件请用 /file/upload 或 /file/uploadBatch 接口");
        }

        return results;
    }

    // ============== 只上传COS ==============

    private CosUploadResult doUploadToCos(MultipartFile file, Long userId) {
        try {
            validateFileSize(file.getSize());
            String originalName = file.getOriginalFilename();
            String fileType = getFileType(originalName);
            String fileName = generateFileName(originalName);
            String cosPath = generateCosPath(userId, fileName);

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            PutObjectRequest putRequest = new PutObjectRequest(
                    cosConfig.getBucket(), cosPath, file.getInputStream(), metadata
            );
            cosClient.putObject(putRequest);

            CosUploadResult result = new CosUploadResult();
            result.setFileName(fileName);
            result.setOriginalName(originalName);
            result.setFileSize(file.getSize());
            result.setFileUrl(generateFileUrl(cosPath));
            result.setFileType(fileType);
            result.setCosPath(cosPath);
            return result;
        } catch (Exception e) {
            throw new BizException("COS上传失败：" + e.getMessage());
        }
    }

    private CosUploadResult doUploadBase64ToCos(String base64, String fileName, Long userId) {
        try {
            byte[] fileBytes = Base64.getDecoder().decode(base64);
            long fileSize = fileBytes.length;
            validateFileSize(fileSize);

            String fileType = getFileType(fileName);
            String generatedFileName = generateFileName(fileName);
            String cosPath = generateCosPath(userId, generatedFileName);

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(fileSize);
            metadata.setContentType(getMimeType(fileType));

            PutObjectRequest putRequest = new PutObjectRequest(
                    cosConfig.getBucket(), cosPath, new ByteArrayInputStream(fileBytes), metadata
            );
            cosClient.putObject(putRequest);

            CosUploadResult result = new CosUploadResult();
            result.setFileName(generatedFileName);
            result.setOriginalName(fileName);
            result.setFileSize(fileSize);
            result.setFileUrl(generateFileUrl(cosPath));
            result.setFileType(fileType);
            result.setCosPath(cosPath);
            return result;
        } catch (Exception e) {
            throw new BizException("Base64上传COS失败：" + e.getMessage());
        }
    }

    private CosUploadResult doUploadUrlToCos(String url, String fileName, Long userId) {
        try {
            java.net.URL fileUrl = new java.net.URL(url);
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) fileUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(30000);
            connection.connect();

            if (connection.getResponseCode() != 200) {
                throw new BizException("下载文件失败，HTTP状态码：" + connection.getResponseCode());
            }

            byte[] fileBytes;
            try (InputStream inputStream = connection.getInputStream()) {
                fileBytes = inputStream.readAllBytes();
            }
            connection.disconnect();

            long fileSize = fileBytes.length;
            validateFileSize(fileSize);

            if (fileName == null || fileName.isEmpty()) {
                String urlPath = fileUrl.getPath();
                fileName = urlPath.substring(urlPath.lastIndexOf("/") + 1);
                if (fileName.isEmpty()) {
                    fileName = "download_" + System.currentTimeMillis();
                }
            }

            String fileType = getFileType(fileName);
            String generatedFileName = generateFileName(fileName);
            String cosPath = generateCosPath(userId, generatedFileName);

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(fileSize);
            metadata.setContentType(getMimeType(fileType));

            PutObjectRequest putRequest = new PutObjectRequest(
                    cosConfig.getBucket(), cosPath, new ByteArrayInputStream(fileBytes), metadata
            );
            cosClient.putObject(putRequest);

            CosUploadResult result = new CosUploadResult();
            result.setFileName(generatedFileName);
            result.setOriginalName(fileName);
            result.setFileSize(fileSize);
            result.setFileUrl(generateFileUrl(cosPath));
            result.setFileType(fileType);
            result.setCosPath(cosPath);
            return result;
        } catch (Exception e) {
            throw new BizException("URL上传COS失败：" + e.getMessage());
        }
    }

    // ============== 工具方法 ==============

    private FileRecord saveFileRecord(CosUploadResult cosResult, Long userId, Long folderId, String mimeType) {
        FileRecord record = new FileRecord();
        record.setId(SnowflakeUtils.nextId(BizCodeEnum.FILE));
        record.setUserId(userId);
        record.setFileName(cosResult.getFileName());
        record.setOriginalName(cosResult.getOriginalName());
        record.setFileType(cosResult.getFileType());
        record.setMimeType(mimeType);
        record.setFileSize(cosResult.getFileSize());
        record.setCosPath(cosResult.getCosPath());
        record.setFileUrl(cosResult.getFileUrl());
        // folderId = 0 视为根目录，存数据库用 null（跟目录体系保持一致）
        Long realFolderId = (folderId != null && folderId == 0L) ? null : folderId;
        record.setFolderId(realFolderId);
        record.setStatus(1);
        record.setCreateTime(java.time.LocalDateTime.now());
        fileRecordMapper.insert(record);
        return record;
    }

    private FileUploadVo toFileUploadVo(CosUploadResult cosResult, Long fileId) {
        FileUploadVo vo = new FileUploadVo();
        vo.setFileId(fileId);
        vo.setFileName(cosResult.getFileName());
        vo.setOriginalName(cosResult.getOriginalName());
        vo.setFileSize(cosResult.getFileSize());
        vo.setFileUrl(cosResult.getFileUrl());
        vo.setFileType(cosResult.getFileType());
        return vo;
    }

    private String getMimeType(String fileType) {
        if (fileType == null || fileType.isEmpty()) {
            return "application/octet-stream";
        }
        return switch (fileType.toLowerCase()) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            case "mp4" -> "video/mp4";
            case "mp3" -> "audio/mpeg";
            case "pdf" -> "application/pdf";
            case "doc" -> "application/msword";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls" -> "application/vnd.ms-excel";
            case "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            default -> "application/octet-stream";
        };
    }

    private void validateFileSize(long fileSize) {
        if (fileSize < cosConfig.getMinFileSize()) {
            throw new BizException("文件大小不能小于 " + formatFileSize(cosConfig.getMinFileSize()));
        }
        if (fileSize > cosConfig.getMaxFileSize()) {
            throw new BizException("文件大小不能超过 " + formatFileSize(cosConfig.getMaxFileSize()));
        }
    }

    private String formatFileSize(long size) {
        if (size < 1024) {
            return size + "B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2fKB", size / 1024.0);
        } else {
            return String.format("%.2fMB", size / (1024.0 * 1024));
        }
    }

    private String getFileType(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }

    private String generateFileName(String originalName) {
        String fileType = getFileType(originalName);
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return uuid + (fileType.isEmpty() ? "" : "." + fileType);
    }

    /**
     * 生成 COS 存储路径：
     * userId = null → simple/yyyy/MM/dd/xxx
     * userId != null → user/{userId}/yyyy/MM/dd/xxx
     */
    private String generateCosPath(Long userId, String fileName) {
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        if (userId == null) {
            return String.format("simple/%s/%s", datePath, fileName);
        }
        return String.format("user/%d/%s/%s", userId, datePath, fileName);
    }

    private String generateFileUrl(String cosPath) {
        if (cosConfig.getDomain() != null && !cosConfig.getDomain().isEmpty()) {
            return cosConfig.getDomain() + "/" + cosPath;
        }
        return String.format("https://%s.cos.%s.myqcloud.com/%s",
                cosConfig.getBucket(), cosConfig.getRegion(), cosPath);
    }

    @Data
    private static class CosUploadResult {
        private String fileName;
        private String originalName;
        private Long fileSize;
        private String fileUrl;
        private String fileType;
        private String cosPath;
    }
}
