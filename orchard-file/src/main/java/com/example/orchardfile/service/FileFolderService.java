package com.example.orchardfile.service;

import com.example.orchardfile.vo.FileDetailVo;
import com.example.orchardfile.vo.FolderTreeVo;

import java.util.List;

/**
 * 文件目录管理服务
 */
public interface FileFolderService {

    /**
     * 创建文件夹
     * @param folderName 文件夹名称
     * @param parentId 父文件夹ID（可选）
     * @param userId 用户ID
     * @return 文件夹ID
     */
    Long createFolder(String folderName, Long parentId, Long userId);

    /**
     * 获取用户的文件夹树
     * @param userId 用户ID
     * @return 文件夹树
     */
    List<FolderTreeVo> getFolderTree(Long userId);

    /**
     * 获取文件夹下的文件列表
     * @param folderId 文件夹ID
     * @param userId 用户ID
     * @return 文件列表
     */
    List<FileDetailVo> getFilesByFolder(Long folderId, Long userId);

    /**
     * 删除文件夹
     * @param folderId 文件夹ID
     * @param userId 用户ID
     */
    void deleteFolder(Long folderId, Long userId);

    /**
     * 删除文件
     * @param fileId 文件ID
     * @param userId 用户ID
     */
    void deleteFile(Long fileId, Long userId);
}
