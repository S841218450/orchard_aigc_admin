package com.example.orchardfile.service.impl;

import com.example.orchardcommon.business.SnowflakeId.BizCodeEnum;
import com.example.orchardcommon.business.SnowflakeId.SnowflakeUtils;
import com.example.orchardfile.config.CosConfig;
import com.example.orchardfile.entity.FileRecord;
import com.example.orchardfile.mapper.FileRecordMapper;
import com.example.orchardfile.service.FileUploadService;
import com.example.orchardfile.vo.FileUploadVo;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.UploadPartRequest;
import com.qcloud.cos.model.UploadPartResult;
import com.qcloud.cos.model.InitiateMultipartUploadRequest;
import com.qcloud.cos.model.InitiateMultipartUploadResult;
import com.qcloud.cos.model.CompleteMultipartUploadRequest;
import com.qcloud.cos.model.PartETag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 文件上传服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileUploadServiceImpl implements FileUploadService {

    private final COSClient cosClient;
    private final CosConfig cosConfig;
    private final FileRecordMapper fileRecordMapper;

    @Override
    public FileUploadVo uploadFile(MultipartFile file, Long userId, Long folderId) {
        try {
            // 校验文件大小
            validateFileSize(file.getSize());

            // 判断是否需要分块上传
            if (file.getSize() > cosConfig.getMultipartThreshold()) {
                return uploadMultipartFile(file, userId, folderId);
            }

            // 生成文件信息
            String originalName = file.getOriginalFilename();
            String fileType = getFileType(originalName);
            String fileName = generateFileName(originalName);
            String cosPath = generateCosPath(userId, fileName);

            // 上传到 COS
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            PutObjectRequest putRequest = new PutObjectRequest(
                    cosConfig.getBucket(),
                    cosPath,
                    file.getInputStream(),
                    metadata
            );
            PutObjectResult putResult = cosClient.putObject(putRequest);

            // 生成访问 URL
            String fileUrl = generateFileUrl(cosPath);

            // 保存文件记录
            FileRecord record = new FileRecord();
            record.setId(SnowflakeUtils.nextId(BizCodeEnum.FILE));
            record.setUserId(userId);
            record.setFileName(fileName);
            record.setOriginalName(originalName);
            record.setFileType(fileType);
            record.setMimeType(file.getContentType());
            record.setFileSize(file.getSize());
            record.setCosPath(cosPath);
            record.setFileUrl(fileUrl);
            record.setFolderId(folderId);
            record.setStatus(1);
            record.setCreateTime(java.time.LocalDateTime.now());

            fileRecordMapper.insert(record);

            // 返回结果
            FileUploadVo vo = new FileUploadVo();
            vo.setFileId(record.getId());
            vo.setFileName(fileName);
            vo.setOriginalName(originalName);
            vo.setFileSize(file.getSize());
            vo.setFileUrl(fileUrl);
            vo.setFileType(fileType);

            log.info("文件上传成功：userId={}, fileName={}, size={}", userId, fileName, file.getSize());
            return vo;

        } catch (Exception e) {
            log.error("文件上传失败：userId={}, error={}", userId, e.getMessage(), e);
            throw new RuntimeException("文件上传失败：" + e.getMessage());
        }
    }

    @Override
    public FileUploadVo uploadMultipartFile(MultipartFile file, Long userId, Long folderId) {
        try {
            // 校验文件大小
            validateFileSize(file.getSize());

            // 生成文件信息
            String originalName = file.getOriginalFilename();
            String fileType = getFileType(originalName);
            String fileName = generateFileName(originalName);
            String cosPath = generateCosPath(userId, fileName);

            // 初始化分块上传
            InitiateMultipartUploadRequest initRequest = new InitiateMultipartUploadRequest(
                    cosConfig.getBucket(),
                    cosPath
            );
            InitiateMultipartUploadResult initResult = cosClient.initiateMultipartUpload(initRequest);
            String uploadId = initResult.getUploadId();

            // 分块上传
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

            // 完成分块上传
            CompleteMultipartUploadRequest completeRequest = new CompleteMultipartUploadRequest(
                    cosConfig.getBucket(),
                    cosPath,
                    uploadId,
                    partETags
            );
            cosClient.completeMultipartUpload(completeRequest);

            // 生成访问 URL
            String fileUrl = generateFileUrl(cosPath);

            // 保存文件记录
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
            record.setFolderId(folderId);
            record.setStatus(1);
            record.setCreateTime(java.time.LocalDateTime.now());

            fileRecordMapper.insert(record);

            // 返回结果
            FileUploadVo vo = new FileUploadVo();
            vo.setFileId(record.getId());
            vo.setFileName(fileName);
            vo.setOriginalName(originalName);
            vo.setFileSize(fileSize);
            vo.setFileUrl(fileUrl);
            vo.setFileType(fileType);

            log.info("大文件分块上传成功：userId={}, fileName={}, size={}", userId, fileName, fileSize);
            return vo;

        } catch (Exception e) {
            log.error("大文件分块上传失败：userId={}, error={}", userId, e.getMessage(), e);
            throw new RuntimeException("大文件上传失败：" + e.getMessage());
        }
    }

    /**
     * 校验文件大小
     */
    private void validateFileSize(long fileSize) {
        if (fileSize < cosConfig.getMinFileSize()) {
            throw new RuntimeException("文件大小不能小于 " + formatFileSize(cosConfig.getMinFileSize()));
        }
        if (fileSize > cosConfig.getMaxFileSize()) {
            throw new RuntimeException("文件大小不能超过 " + formatFileSize(cosConfig.getMaxFileSize()));
        }
    }

    /**
     * 格式化文件大小
     */
    private String formatFileSize(long size) {
        if (size < 1024) {
            return size + "B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2fKB", size / 1024.0);
        } else {
            return String.format("%.2fMB", size / (1024.0 * 1024));
        }
    }

    /**
     * 获取文件类型
     */
    private String getFileType(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * 生成文件名
     */
    private String generateFileName(String originalName) {
        String fileType = getFileType(originalName);
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return uuid + (fileType.isEmpty() ? "" : "." + fileType);
    }

    /**
     * 生成 COS 存储路径
     */
    private String generateCosPath(Long userId, String fileName) {
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        return String.format("user/%d/%s/%s", userId, datePath, fileName);
    }

    /**
     * 生成文件访问 URL
     */
    private String generateFileUrl(String cosPath) {
        if (cosConfig.getDomain() != null && !cosConfig.getDomain().isEmpty()) {
            return cosConfig.getDomain() + "/" + cosPath;
        }
        return String.format("https://%s.cos.%s.myqcloud.com/%s",
                cosConfig.getBucket(),
                cosConfig.getRegion(),
                cosPath);
    }
}
