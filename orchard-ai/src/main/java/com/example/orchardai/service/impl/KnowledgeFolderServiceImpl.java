package com.example.orchardai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.orchardai.dto.KnowledgeFolderCreateDto;
import com.example.orchardai.entity.KnowledgeDoc;
import com.example.orchardai.entity.KnowledgeFolder;
import com.example.orchardai.mapper.KnowledgeDocMapper;
import com.example.orchardai.mapper.KnowledgeFolderMapper;
import com.example.orchardai.service.KnowledgeFolderService;
import com.example.orchardai.vo.KnowledgeFolderTreeVo;
import com.example.orchardcommon.business.SnowflakeId.BizCodeEnum;
import com.example.orchardcommon.business.SnowflakeId.SnowflakeUtils;
import com.example.orchardcommon.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeFolderServiceImpl implements KnowledgeFolderService {

    private final KnowledgeFolderMapper folderMapper;
    private final KnowledgeDocMapper docMapper;

    @Override
    public Long createFolder(KnowledgeFolderCreateDto dto, Long userId) {
        Long realParentId = (dto.getParentId() != null && dto.getParentId() == 0L) ? null : dto.getParentId();

        LambdaQueryWrapper<KnowledgeFolder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeFolder::getUserId, userId)
               .eq(KnowledgeFolder::getFolderName, dto.getFolderName())
               .eq(realParentId == null, KnowledgeFolder::getParentId, null)
               .eq(realParentId != null, KnowledgeFolder::getParentId, realParentId)
               .eq(KnowledgeFolder::getStatus, 1);

        if (folderMapper.selectCount(wrapper) > 0) {
            throw new BizException("该目录下已存在同名文件夹");
        }

        KnowledgeFolder folder = new KnowledgeFolder();
        folder.setId(SnowflakeUtils.nextId(BizCodeEnum.AIDOC));
        folder.setUserId(userId);
        folder.setFolderName(dto.getFolderName());
        folder.setParentId(realParentId);
        folder.setSort(0);
        folder.setStatus(1);
        folder.setCreateTime(LocalDateTime.now());
        folder.setUpdateTime(null);
        folderMapper.insert(folder);
        log.info("创建知识库目录成功：userId={}, folderName={}, parentId={}", userId, dto.getFolderName(), dto.getParentId());
        return folder.getId();
    }

    @Override
    public List<KnowledgeFolderTreeVo> getFolderTree(Long userId) {
        LambdaQueryWrapper<KnowledgeFolder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeFolder::getUserId, userId)
               .eq(KnowledgeFolder::getStatus, 1)
               .orderByAsc(KnowledgeFolder::getSort);

        List<KnowledgeFolder> folders = folderMapper.selectList(wrapper);

        // 统计每个目录下的文档数量
        LambdaQueryWrapper<KnowledgeDoc> docWrapper = new LambdaQueryWrapper<>();
        docWrapper.eq(KnowledgeDoc::getUserId, userId)
                  .eq(KnowledgeDoc::getDeleted, 0);
        Map<Long, Long> docCountMap = docMapper.selectList(docWrapper).stream()
                .collect(Collectors.groupingBy(
                        doc -> doc.getFolderId() == null ? 0L : doc.getFolderId(),
                        Collectors.counting()));

        List<KnowledgeFolderTreeVo> allFolders = folders.stream()
                .map(folder -> convertToTreeVo(folder, docCountMap))
                .toList();

        Map<Long, List<KnowledgeFolderTreeVo>> parentMap = allFolders.stream()
                .collect(Collectors.groupingBy(f -> f.getParentId() == null ? 0L : f.getParentId()));

        for (KnowledgeFolderTreeVo folder : allFolders) {
            List<KnowledgeFolderTreeVo> children = parentMap.get(folder.getId());
            if (children != null && !children.isEmpty()) {
                folder.setChildren(children);
            }
        }

        List<KnowledgeFolderTreeVo> rootChildren = parentMap.getOrDefault(0L, new ArrayList<>());

        KnowledgeFolderTreeVo root = new KnowledgeFolderTreeVo();
        root.setId(0L);
        root.setFolderName("根目录");
        root.setParentId(null);
        root.setDocCount(docCountMap.getOrDefault(0L, 0L));
        root.setChildren(rootChildren);

        return List.of(root);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFolder(Long folderId, Long userId) {
        if (folderId == null || folderId == 0L) {
            throw new BizException("根目录不允许删除");
        }

        KnowledgeFolder folder = folderMapper.selectById(folderId);
        if (folder == null || !folder.getUserId().equals(userId)) {
            throw new BizException("文件夹不存在或无权访问");
        }

        LambdaQueryWrapper<KnowledgeDoc> docWrapper = new LambdaQueryWrapper<>();
        docWrapper.eq(KnowledgeDoc::getFolderId, folderId)
                  .eq(KnowledgeDoc::getDeleted, 0);
        if (docMapper.selectCount(docWrapper) > 0) {
            throw new BizException("该目录下还有文档，请先删除文档");
        }

        LambdaQueryWrapper<KnowledgeFolder> subWrapper = new LambdaQueryWrapper<>();
        subWrapper.eq(KnowledgeFolder::getParentId, folderId)
                  .eq(KnowledgeFolder::getStatus, 1);
        if (folderMapper.selectCount(subWrapper) > 0) {
            throw new BizException("该目录下还有子目录，请先删除子目录");
        }

        folder.setStatus(0);
        folder.setUpdateTime(LocalDateTime.now());
        folderMapper.updateById(folder);
        log.info("删除知识库目录成功：userId={}, folderId={}", userId, folderId);
    }

    private KnowledgeFolderTreeVo convertToTreeVo(KnowledgeFolder folder, Map<Long, Long> docCountMap) {
        KnowledgeFolderTreeVo vo = new KnowledgeFolderTreeVo();
        vo.setId(folder.getId());
        vo.setFolderName(folder.getFolderName());
        vo.setParentId(folder.getParentId());
        vo.setDocCount(docCountMap.getOrDefault(folder.getId(), 0L));
        vo.setCreateTime(folder.getCreateTime());
        vo.setChildren(new ArrayList<>());
        return vo;
    }
}
