package com.example.orchardai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

/**
 * Agent（Python）端 文档分割 + 向量化入库 请求体
 * <p>
 * Agent 接口契约：POST /api/v1/knowledge_base/internal/documents
 * 字段序列化统一为 snake_case（doc_id/doc_name/file_url/kb_id），
 * 通过 @JsonProperty 映射，Java 侧代码风格保持 camelCase。
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AgentIngestRequest {

    /** 必须：Java 端文档唯一 ID（MySQL 主键或 Snowflake） */
    @JsonProperty("doc_id")
    private String docId;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("doc_name")
    private String docName;

    /** 二选一优先：COS/OSS 可直接 GET 的文件 URL */
    @JsonProperty("file_url")
    private String fileUrl;

    /** 二选一兜底：纯文本（仅小 TXT/MD 才用），和 fileUrl 至少一个非空 */
    private String content;

    /** 可选：知识库 ID，不传默认 default */
    @JsonProperty("kb_id")
    private String kbId;

    /** 可选：只传 folder_id，其他一概不传 */
    private Map<String, Object> metadata;
}
