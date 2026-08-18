package com.example.orchardai.client;

import com.example.orchardai.dto.AgentBatchDeleteRequest;
import com.example.orchardai.dto.AgentIngestRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import com.example.orchardcommon.exception.BizException;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Agent（Python）端 API 客户端
 * <p>
 * 对称于 Agent 端的 JavaApiClient：统一内部服务密钥鉴权（X-Service-Key）、复用连接池。
 * 使用 Spring 6.1 RestClient（RestTemplate 的官方替代，spring-web 自带，零额外依赖）。
 * 配置：
 * - agent.base-url      Agent 服务地址（默认 http://localhost:8000）
 * - agent.service-key   发给 Agent 的密钥，必须与 Agent 端 JAVA_INTERNAL_TOKEN 一致
 */
@Slf4j
@Component
public class AgentApiClient {

    /** Agent 文档向量化入库接口 */
    private static final String INGEST_PATH = "knowledge-base/internal/documents";
    /** Agent 会话记忆清除接口 */
    private static final String MEMORY_CLEAR_PATH = "/internal/memory/clear";
    /** Agent 消息记忆删除接口 */
    private static final String MEMORY_MESSAGES_DELETE_PATH = "/internal/memory/messages/delete";
    /** Agent 会话标题生成接口 */
    private static final String MEMORY_TITLE_PATH = "/internal/memory/title";

    private final RestClient restClient;

    public AgentApiClient(
            @Value("${agent.base-url:http://134.175.217.240/ai-api/v1}") String baseUrl,
            @Value("${agent.service-key:${internal.service-key:orchard-agent-secret-key}}") String serviceKey,
            RestClient.Builder builder) {
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(5))
                        .build());
        requestFactory.setReadTimeout(Duration.ofSeconds(30));

        this.restClient = builder
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .defaultHeader("X-Service-Key", serviceKey)
                .build();
    }

    /**
     * 提交文档分割 + 向量化任务
     * 提交后立即返回，Agent 内部异步处理，处理结果通过回调 PUT /api/ai/knowledge/status 更新。
     */
    public void ingestDocument(AgentIngestRequest request) {
        log.info("向 Agent 提交向量化任务：docId={}, docName={}", request.getDocId(), request.getDocName());
        restClient.post()
                .uri(INGEST_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toBodilessEntity();
        log.info("已向 Agent 提交向量化任务：docId={}, docName={}", request.getDocId(), request.getDocName());
    }

    /**
     * 删除 Agent 向量库中的单个文档（物理删 hard / 软删 soft）
     * Agent 端向量不存在（404）视为删除成功，保证幂等。
     *
     * @param docId 文档 ID
     */
    public void deleteDocument(String docId) {
        try {
            restClient.delete()
                    .uri(INGEST_PATH + "/{docId}", docId)
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Agent 知识库向量不存在，忽略删除：docId={}", docId);
            return;
        }
        log.info("已删除 Agent 知识库向量：docId={}", docId);
    }

    /**
     * 通知 Agent 清除会话记忆（删除会话时同步调用）
     * Agent 删除失败时抛 {@link BizException} 阻断本地删除，保证会话记忆不残留。
     *
     * @param threadId 会话ID（对应 Agent 的 thread_id）
     */
    public void clearMemory(String threadId) {
        try {
            restClient.post()
                    .uri(MEMORY_CLEAR_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("threadId", threadId))
                    .retrieve()
                    .toBodilessEntity();
            log.info("已通知 Agent 清除会话记忆：threadId={}", threadId);
        } catch (Exception e) {
            log.error("通知 Agent 清除会话记忆失败：threadId={}", threadId, e);
            throw new BizException("Agent 会话记忆清除失败，请稍后重试");
        }
    }

    /**
     * 通知 Agent 异步生成会话标题（新建会话携带第一条消息调用）
     * Agent 端 LLM 提炼标题后回调 Java 保存，本接口立即返回、不阻塞首条对话。
     * 失败仅记日志不抛异常，不影响会话创建。
     *
     * @param threadId 会话ID
     * @param question 第一条用户消息
     */
    public void generateTitle(String threadId, String question) {
        try {
            restClient.post()
                    .uri(MEMORY_TITLE_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("threadId", threadId, "question", question))
                    .retrieve()
                    .toBodilessEntity();
            log.info("已通知 Agent 生成会话标题：threadId={}", threadId);
        } catch (Exception e) {
            log.error("通知 Agent 生成会话标题失败：threadId={}", threadId, e);
        }
    }

    /**
     * 通知 Agent 删除指定消息的记忆（删除消息时同步调用）
     * 失败仅记日志不抛异常，避免 Agent 不可用时阻断用户删除消息。
     *
     * @param messageIds 用户消息ID列表
     */
    public void deleteMessages(List<String> messageIds) {
        try {
            restClient.post()
                    .uri(MEMORY_MESSAGES_DELETE_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("messageIds", messageIds))
                    .retrieve()
                    .toBodilessEntity();
            log.info("已通知 Agent 删除消息记忆：messageIds={}", messageIds);
        } catch (Exception e) {
            log.error("通知 Agent 删除消息记忆失败：messageIds={}", messageIds, e);
        }
    }

}
