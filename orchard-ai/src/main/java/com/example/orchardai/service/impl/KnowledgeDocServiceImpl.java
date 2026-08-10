package com.example.orchardai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.orchardai.client.AgentApiClient;
import com.example.orchardai.client.AgentIngestTrigger;
import com.example.orchardai.dto.KnowledgeDocQueryDto;
import com.example.orchardai.dto.KnowledgeDocStatusUpdateDto;
import com.example.orchardai.dto.KnowledgeDocVo;
import com.example.orchardai.entity.KnowledgeDoc;
import com.example.orchardai.enums.DocStatusEnum;
import com.example.orchardai.mapper.KnowledgeDocMapper;
import com.example.orchardai.service.KnowledgeDocService;
import com.example.orchardcommon.business.SnowflakeId.BizCodeEnum;
import com.example.orchardcommon.business.SnowflakeId.SnowflakeUtils;
import com.example.orchardcommon.exception.BizException;
import com.example.orchardcommon.result.PageResult;
import com.example.orchardfile.service.FileUploadService;
import com.example.orchardfile.vo.FileUploadVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeDocServiceImpl implements KnowledgeDocService {

    private final KnowledgeDocMapper knowledgeDocMapper;
    private final FileUploadService fileUploadService;
    private final AgentIngestTrigger agentIngestTrigger;
    private final AgentApiClient agentApiClient;

    @Override
    public KnowledgeDocVo upload(MultipartFile file, Long folderId, Long userId) {
        if (userId == null) {
            throw new BizException("用户ID不能为空");
        }
        if (file == null || file.isEmpty()) {
            throw new BizException("上传文件不能为空");
        }

        // 1. 直接上传到COS（folderId=null → 只传COS，不写 file_record 表，因为知识库文档信息存 knowledge_doc）
        FileUploadVo cosResult = fileUploadService.uploadFile(file, userId, null);

        // 2. 保存到 knowledge_doc
        KnowledgeDoc doc = new KnowledgeDoc();
        doc.setId(SnowflakeUtils.nextId(BizCodeEnum.KB_DOC));
        doc.setUserId(userId);
        Long normalizedFolderId = (folderId == null || folderId == 0L) ? null : folderId;
        doc.setFolderId(normalizedFolderId);
        doc.setFileName(cosResult.getOriginalName());
        doc.setFileUrl(cosResult.getFileUrl());
        doc.setFileSize(cosResult.getFileSize());
        doc.setDocType(cosResult.getFileType());
        doc.setStatus(DocStatusEnum.PENDING.getCode());
        doc.setCreateTime(LocalDateTime.now());
        doc.setUpdateTime(LocalDateTime.now());

        knowledgeDocMapper.insert(doc);
        log.info("知识库文档上传成功：id={}, fileName={}, userId={}, folderId={}, fileSize={}",
                doc.getId(), doc.getFileName(), userId, folderId, doc.getFileSize());

        // 异步触发 Agent 分割 + 向量化入库（不阻塞上传响应，Agent 处理完回调 /status）
        agentIngestTrigger.trigger(doc);

        return toVo(doc);
    }

    @Override
    public void updateStatus(KnowledgeDocStatusUpdateDto dto) {
        KnowledgeDoc doc = knowledgeDocMapper.selectById(dto.getId());
        if (doc == null) {
            throw new BizException("文档不存在");
        }
        doc.setStatus(dto.getStatus());
        if (dto.getChunkCount() != null) {
            doc.setChunkCount(dto.getChunkCount());
        }
        if (dto.getTokenCount() != null) {
            doc.setTokenCount(dto.getTokenCount());
        }
        if (dto.getFileSize() != null && doc.getFileSize() == null) {
            doc.setFileSize(dto.getFileSize());
        }
        if (StringUtils.hasText(dto.getErrorMsg())) {
            doc.setErrorMsg(dto.getErrorMsg());
        }
        doc.setUpdateTime(LocalDateTime.now());
        knowledgeDocMapper.updateById(doc);
        log.info("知识库文档状态更新：id={}, status={}", dto.getId(), dto.getStatus());
    }

    @Override
    public PageResult<KnowledgeDocVo> page(KnowledgeDocQueryDto query, Long userId) {
        LambdaQueryWrapper<KnowledgeDoc> wrapper = new LambdaQueryWrapper<>();
        // 数据隔离：强制限定当前登录用户
        wrapper.eq(KnowledgeDoc::getUserId, userId);

        Long folderId = query.getFolderId();
        if (folderId != null) {
            if (folderId.equals(0L)) {
                // 入参0：只查根目录，数据库folder_id为空的数据
                wrapper.isNull(KnowledgeDoc::getFolderId);
            } else {
                // 传入正常文件夹ID：精准匹配该文件夹
                wrapper.eq(KnowledgeDoc::getFolderId, folderId);
            }
        }
        // folderId 为 null：不拼接文件夹条件，查询用户全部文件

        // 状态筛选、文件名模糊搜索、排序逻辑保持不变
        wrapper.eq(query.getStatus() != null, KnowledgeDoc::getStatus, query.getStatus())
                .like(StringUtils.hasText(query.getQuery()), KnowledgeDoc::getFileName, query.getQuery())
                .orderByDesc(KnowledgeDoc::getCreateTime);

        Page<KnowledgeDoc> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<KnowledgeDoc> result = knowledgeDocMapper.selectPage(page, wrapper);
        return PageResult.of(result, this::toVo);
    }

    @Override
    public void delete(Long id, Long userId) {
        KnowledgeDoc doc = knowledgeDocMapper.selectById(id);
        if (doc == null) {
            throw new BizException("文档不存在");
        }
        if (!doc.getUserId().equals(userId)) {
            throw new BizException("无权删除该文档");
        }
        //没入库就不同步删向量库
        if(doc.getStatus() == DocStatusEnum.COMPLETED.getCode()){
            // 同步删除 Agent 向量库（hard 物理删），失败则抛异常终止本地删除，保证两端数据一致
            agentApiClient.deleteDocument(doc.getId().toString());
        }

        knowledgeDocMapper.deleteById(id);
        log.info("知识库文档删除：id={}, userId={}", id, userId);
    }

    @Override
    public void retry(Long id, Long userId) {
        KnowledgeDoc doc = knowledgeDocMapper.selectById(id);
        if (doc == null) {
            throw new BizException("文档不存在");
        }
        if (!doc.getUserId().equals(userId)) {
            throw new BizException("无权重试该文档");
        }
        int current = doc.getStatus() == null ? DocStatusEnum.PENDING.getCode() : doc.getStatus();
        if (current == DocStatusEnum.PROCESSING.getCode() || current == DocStatusEnum.COMPLETED.getCode()) {
            throw new BizException("仅待处理或失败的文档可以重试");
        }
        // 重置为待处理并清空旧失败信息，重新触发 Agent 向量化
        doc.setStatus(DocStatusEnum.PENDING.getCode());
        doc.setUserId(userId);
        doc.setErrorMsg(null);
        doc.setChunkCount(null);
        doc.setTokenCount(null);
        doc.setUpdateTime(LocalDateTime.now());
        knowledgeDocMapper.updateById(doc);

        agentIngestTrigger.trigger(doc);
        log.info("知识库文档重试向量化：id={}, userId={}", id, userId);
    }

    @Override
    public KnowledgeDocVo getById(Long id, Long userId) {
        KnowledgeDoc doc = knowledgeDocMapper.selectById(id);
        if (doc == null) {
            throw new BizException("文档不存在");
        }
        if (!doc.getUserId().equals(userId)) {
            throw new BizException("无权查看该文档");
        }
        return toVo(doc);
    }

    private KnowledgeDocVo toVo(KnowledgeDoc doc) {
        KnowledgeDocVo vo = new KnowledgeDocVo();
        vo.setId(doc.getId());
        vo.setUserId(doc.getUserId());
        vo.setFolderId(doc.getFolderId() == null ? 0L : doc.getFolderId());
        vo.setFileName(doc.getFileName());
        vo.setFileUrl(doc.getFileUrl());
        vo.setFileSize(doc.getFileSize());
        vo.setDocType(doc.getDocType());
        vo.setStatus(doc.getStatus());
        vo.setChunkCount(doc.getChunkCount());
        vo.setTokenCount(doc.getTokenCount());
        vo.setErrorMsg(doc.getErrorMsg());
        vo.setCreateTime(doc.getCreateTime());
        vo.setUpdateTime(doc.getUpdateTime());
        return vo;
    }
}
