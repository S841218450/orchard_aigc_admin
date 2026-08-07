package com.example.orchardfile.service;

import com.example.orchardfile.dto.FileUnifiedUploadDto;
import com.example.orchardfile.dto.FileUploadBase64Dto;
import com.example.orchardfile.dto.FileUploadUrlDto;
import com.example.orchardfile.vo.FileUploadVo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件上传服务
 * 规则：folderId != null（含 0 = 根目录）→ 写 file_record 数据库并绑定该目录；
 *       folderId == null → 只上传到 COS，返回 fileUrl，不写数据库。
 */
public interface FileUploadService {

    /**
     * 上传二进制文件（单）
     *
     * @param folderId null=只拿URL；非null（含0）=写文件系统库绑定目录
     */
    FileUploadVo uploadFile(MultipartFile file, Long userId, Long folderId);

    /**
     * 批量上传二进制文件
     */
    List<FileUploadVo> uploadFileBatch(List<MultipartFile> files, Long userId, Long folderId);

    /**
     * 分块上传大文件
     */
    FileUploadVo uploadMultipartFile(MultipartFile file, Long userId, Long folderId);

    /**
     * Base64 上传
     */
    FileUploadVo uploadFileBase64(String base64, String fileName, Long userId, Long folderId);

    /**
     * 批量 Base64 上传
     *
     * @param defaultFolderId 全局默认 folderId；每个文件自己的 folderId 优先
     */
    List<FileUploadVo> uploadFileBase64Batch(List<FileUploadBase64Dto> fileList, Long defaultUserId, Long defaultFolderId);

    /**
     * URL 上传
     */
    FileUploadVo uploadFileByUrl(String url, String fileName, Long userId, Long folderId);

    /**
     * 批量 URL 上传
     *
     * @param defaultFolderId 全局默认 folderId；每个文件自己的 folderId 优先
     */
    List<FileUploadVo> uploadFileByUrlBatch(List<FileUploadUrlDto> fileList, Long defaultUserId, Long defaultFolderId);

    /**
     * 统一上传入口（纯 JSON body：Base64 / URL，单文件或批量）
     * 二进制文件请用 /upload 或 /uploadBatch
     */
    List<FileUploadVo> unifiedUpload(FileUnifiedUploadDto dto, Long userId);
}
