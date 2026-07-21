package com.example.orchardfile.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.orchardcommon.business.SnowflakeId.BizCodeEnum;
import com.example.orchardcommon.business.SnowflakeId.SnowflakeUtils;
import com.example.orchardfile.entity.FileFolder;
import com.example.orchardfile.entity.FileRecord;
import com.example.orchardfile.mapper.FileFolderMapper;
import com.example.orchardfile.mapper.FileRecordMapper;
import com.example.orchardfile.service.FileFolderService;
import com.example.orchardfile.vo.FileDetailVo;
import com.example.orchardfile.vo.FolderTreeVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 文件目录管理服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileFolderServiceImpl implements FileFolderService {

    private final FileFolderMapper fileFolderMapper;
    private final FileRecordMapper fileRecordMapper;

    @Override
    public Long createFolder(String folderName, Long parentId, Long userId) {
        // 检查同级目录下是否已存在同名文件夹
        LambdaQueryWrapper<FileFolder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileFolder::getUserId, userId)
               .eq(FileFolder::getFolderName, folderName)
               .eq(parentId == null, FileFolder::getParentId, null)
               .eq(parentId != null, FileFolder::getParentId, parentId);

        if (fileFolderMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("该目录下已存在同名文件夹");
        }

        // 创建文件夹
        FileFolder folder = new FileFolder();
        folder.setId(SnowflakeUtils.nextId(BizCodeEnum.FILE));
        folder.setUserId(userId);
        folder.setFolderName(folderName);
        folder.setParentId(parentId);
        folder.setSort(0);
        folder.setStatus(1);
        folder.setCreateTime(LocalDateTime.now());

        fileFolderMapper.insert(folder);
        log.info("创建文件夹成功：userId={}, folderName={}, parentId={}", userId, folderName, parentId);

        return folder.getId();
    }

    @Override
    public List<FolderTreeVo> getFolderTree(Long userId) {
        // 查询用户的所有文件夹
        LambdaQueryWrapper<FileFolder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileFolder::getUserId, userId)
               .eq(FileFolder::getStatus, 1)
               .orderByAsc(FileFolder::getSort);

        List<FileFolder> folders = fileFolderMapper.selectList(wrapper);

        // 转换为 VO
        List<FolderTreeVo> allFolders = folders.stream()
                .map(this::convertToFolderTreeVo)
                .collect(Collectors.toList());

        // 构建树形结构
        return buildTree(allFolders);
    }

    @Override
    public List<FileDetailVo> getFilesByFolder(Long folderId, Long userId) {
        LambdaQueryWrapper<FileRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileRecord::getUserId, userId)
               .eq(FileRecord::getStatus, 1)
               .eq(folderId == null, FileRecord::getFolderId, null)
               .eq(folderId != null, FileRecord::getFolderId, folderId)
               .orderByDesc(FileRecord::getCreateTime);

        List<FileRecord> files = fileRecordMapper.selectList(wrapper);

        return files.stream()
                .map(this::convertToFileDetailVo)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFolder(Long folderId, Long userId) {
        // 检查文件夹是否存在
        FileFolder folder = fileFolderMapper.selectById(folderId);
        if (folder == null || !folder.getUserId().equals(userId)) {
            throw new RuntimeException("文件夹不存在或无权访问");
        }

        // 检查文件夹下是否有文件或子文件夹
        LambdaQueryWrapper<FileRecord> fileWrapper = new LambdaQueryWrapper<>();
        fileWrapper.eq(FileRecord::getFolderId, folderId)
                   .eq(FileRecord::getStatus, 1);

        if (fileRecordMapper.selectCount(fileWrapper) > 0) {
            throw new RuntimeException("该文件夹下还有文件，请先删除文件");
        }

        LambdaQueryWrapper<FileFolder> subFolderWrapper = new LambdaQueryWrapper<>();
        subFolderWrapper.eq(FileFolder::getParentId, folderId)
                        .eq(FileFolder::getStatus, 1);

        if (fileFolderMapper.selectCount(subFolderWrapper) > 0) {
            throw new RuntimeException("该文件夹下还有子文件夹，请先删除子文件夹");
        }

        // 删除文件夹
        folder.setStatus(0);
        folder.setUpdateTime(LocalDateTime.now());
        fileFolderMapper.updateById(folder);

        log.info("删除文件夹成功：userId={}, folderId={}", userId, folderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFile(Long fileId, Long userId) {
        // 检查文件是否存在
        FileRecord file = fileRecordMapper.selectById(fileId);
        if (file == null || !file.getUserId().equals(userId)) {
            throw new RuntimeException("文件不存在或无权访问");
        }

        // 删除文件记录
        file.setStatus(0);
        file.setUpdateTime(LocalDateTime.now());
        fileRecordMapper.updateById(file);

        log.info("删除文件成功：userId={}, fileId={}", userId, fileId);
    }

    /**
     * 转换为 FolderTreeVo
     */
    private FolderTreeVo convertToFolderTreeVo(FileFolder folder) {
        FolderTreeVo vo = new FolderTreeVo();
        vo.setId(folder.getId());
        vo.setFolderName(folder.getFolderName());
        vo.setParentId(folder.getParentId());
        vo.setSort(folder.getSort());
        vo.setCreateTime(folder.getCreateTime());
        vo.setChildren(new ArrayList<>());
        return vo;
    }

    /**
     * 转换为 FileDetailVo
     */
    private FileDetailVo convertToFileDetailVo(FileRecord file) {
        FileDetailVo vo = new FileDetailVo();
        vo.setId(file.getId());
        vo.setFileName(file.getFileName());
        vo.setOriginalName(file.getOriginalName());
        vo.setFileType(file.getFileType());
        vo.setFileSize(file.getFileSize());
        vo.setFileUrl(file.getFileUrl());
        vo.setFolderId(file.getFolderId());
        vo.setCreateTime(file.getCreateTime());
        return vo;
    }

    /**
     * 构建树形结构
     */
    private List<FolderTreeVo> buildTree(List<FolderTreeVo> allFolders) {
        Map<Long, List<FolderTreeVo>> parentMap = allFolders.stream()
                .collect(Collectors.groupingBy(f -> f.getParentId() == null ? -1L : f.getParentId()));

        List<FolderTreeVo> rootFolders = parentMap.getOrDefault(-1L, new ArrayList<>());

        for (FolderTreeVo root : rootFolders) {
            buildChildren(root, parentMap);
        }

        return rootFolders;
    }

    /**
     * 递归构建子节点
     */
    private void buildChildren(FolderTreeVo parent, Map<Long, List<FolderTreeVo>> parentMap) {
        List<FolderTreeVo> children = parentMap.get(parent.getId());
        if (children != null && !children.isEmpty()) {
            parent.setChildren(children);
            for (FolderTreeVo child : children) {
                buildChildren(child, parentMap);
            }
        }
    }
}
