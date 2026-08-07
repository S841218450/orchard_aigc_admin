package com.example.orchardai.client;

import com.example.orchardai.dto.AgentBatchDeleteRequest;
import com.example.orchardai.dto.AgentIngestRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

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
    private static final String INGEST_PATH = "/ai-api/v1/knowledge-base/internal/documents";

    private final RestClient restClient;

    public AgentApiClient(
            @Value("${agent.base-url:http://localhost:8000}") String baseUrl,
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

}
