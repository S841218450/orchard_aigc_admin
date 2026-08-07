package com.example.orchardai.dto;

import lombok.Data;

import java.util.List;

/**
 * Agent（Python）端 批量删除向量文档 请求体
 * <p>
 * Agent 删除接口：DELETE /api/v1/knowledge_base/internal/documents/{doc_id}?mode=hard|soft
 * docIds 为空时调用方忽略；mode 不传默认 hard。
 */
@Data
public class AgentBatchDeleteRequest {

    /** 要删的 doc_id 列表 */
    private List<String> docIds;

    /** hard|soft，不传默认 hard */
    private String mode;
}
