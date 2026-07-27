package com.example.orchardfile.service;

import com.example.orchardfile.dto.FileUploadBase64Dto;
import com.example.orchardfile.dto.FileUploadUrlDto;
import com.example.orchardfile.vo.FileUploadVo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件上传服务
 */
public interface FileUploadService {

    /**
     * 上传文件
     * @param file 文件
     * @param userId 用户ID
     * @param folderId 文件夹ID（可选）
     * @return 文件上传结果
     */
    FileUploadVo uploadFile(MultipartFile file, Long userId, Long folderId);

    /**
     * 分块上传大文件
     * @param file 文件
     * @param userId 用户ID
     * @param folderId 文件夹ID（可选）
     * @return 文件上传结果
     */
    FileUploadVo uploadMultipartFile(MultipartFile file, Long userId, Long folderId);

    /**
     * 通过Base64上传文件
     * @param base64 Base64编码的文件内容
     * @param fileName 文件名
     * @param userId 用户ID
     * @param folderId 文件夹ID（可选）
     * @return 文件上传结果
     */
    FileUploadVo uploadFileBase64(String base64, String fileName, Long userId, Long folderId);

    /**
     * 批量通过Base64上传文件
     * @param fileList 文件列表（每个元素包含base64和fileName）
     * @param userId 用户ID
     * @param folderId 文件夹ID（可选）
     * @return 文件上传结果列表
     */
    List<FileUploadVo> uploadFileBase64Batch(List<FileUploadBase64Dto> fileList, Long userId, Long folderId);

    /**
     * 通过URL上传文件
     * @param url 文件URL
     * @param fileName 文件名
     * @param userId 用户ID
     * @param folderId 文件夹ID（可选）
     * @return 文件上传结果
     */
    FileUploadVo uploadFileByUrl(String url, String fileName, Long userId, Long folderId);

    /**
     * 批量通过URL上传文件
     * @param fileList 文件列表（每个元素包含url和fileName）
     * @param userId 用户ID
     * @param folderId 文件夹ID（可选）
     * @return 文件上传结果列表
     */
    List<FileUploadVo> uploadFileByUrlBatch(List<FileUploadUrlDto> fileList, Long userId, Long folderId);
}
