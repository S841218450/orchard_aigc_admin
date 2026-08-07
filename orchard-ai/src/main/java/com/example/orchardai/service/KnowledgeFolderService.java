package com.example.orchardai.service;

import com.example.orchardai.dto.KnowledgeFolderCreateDto;
import com.example.orchardai.vo.KnowledgeFolderTreeVo;

import java.util.List;

public interface KnowledgeFolderService {

    Long createFolder(KnowledgeFolderCreateDto dto, Long userId);

    List<KnowledgeFolderTreeVo> getFolderTree(Long userId);

    void deleteFolder(Long folderId, Long userId);
}
