package com.example.orchardfile.service;

import com.example.orchardfile.vo.FileUploadVo;
import org.springframework.web.multipart.MultipartFile;

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
}
