package com.example.orchardai.client;

import com.example.orchardai.dto.AgentIngestRequest;
import com.example.orchardai.entity.KnowledgeDoc;
import com.example.orchardai.enums.DocStatusEnum;
import com.example.orchardai.mapper.KnowledgeDocMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 文档上传后异步触发 Agent 向量化入库
 * <p>
 * 调用 Agent 提交成功（Agent 端为"提交即返回"异步模式）后把文档置为 PROCESSING，
 * 前端可立即轮询到"处理中"；最终状态由 Agent 回调 /status 更新（COMPLETED/FAILED）。
 * 调用 Agent 失败时把文档置为 FAILED，避免文档永远停留在"待处理"。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AgentIngestTrigger {

    private final AgentApiClient agentApiClient;
    private final KnowledgeDocMapper knowledgeDocMapper;

    @Async("agentTaskExecutor")
    public void trigger(KnowledgeDoc doc) {
        try {
            agentApiClient.ingestDocument(buildRequest(doc));
            updateStatus(doc.getId(), DocStatusEnum.PROCESSING.getCode(), null);
        } catch (Exception e) {
            log.error("调用 Agent 向量化失败：id={}, fileName={}", doc.getId(), doc.getFileName(), e);
            updateStatus(doc.getId(), DocStatusEnum.FAILED.getCode(), "调用 Agent 向量化失败：" + e.getMessage());
        }
    }

    private void updateStatus(Long id, int status, String errorMsg) {
        KnowledgeDoc update = new KnowledgeDoc();
        update.setId(id);
        update.setStatus(status);
        update.setErrorMsg(errorMsg);
        update.setUpdateTime(LocalDateTime.now());
        knowledgeDocMapper.updateById(update);
    }

    private AgentIngestRequest buildRequest(KnowledgeDoc doc) {
        AgentIngestRequest request = new AgentIngestRequest();
        request.setDocId(doc.getId().toString());
        request.setUserId(String.valueOf(doc.getUserId()));
        request.setDocName(doc.getFileName());
        request.setFileUrl(doc.getFileUrl());
        if (doc.getFolderId() != null) {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("folder_id", doc.getFolderId().toString());
            request.setMetadata(metadata);
        }
        return request;
    }
}
