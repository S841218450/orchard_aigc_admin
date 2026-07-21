package com.example.orchardai.service.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.example.orchardai.dto.ChatMessageVo;
import com.example.orchardai.dto.ChatSessionDto;
import com.example.orchardai.entity.ChatMessage;
import com.example.orchardai.service.AiChatService;
import com.example.orchardai.service.ChatMessageService;
import com.example.orchardai.service.ChatSessionService;
import com.example.orchardcommon.business.SnowflakeId.BizCodeEnum;
import com.example.orchardcommon.business.SnowflakeId.SnowflakeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatServiceImpl implements AiChatService {

    @Value("${ai.python.url:http://localhost:8000}")
    private String pythonUrl;


    private final ChatMessageService chatMessageService;
    private final ChatSessionService chatSessionService;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    @Override
    public ChatMessageVo send(Long sessionId, String userMessage) {
        // 没有会话ID就新建会话
        if (sessionId == null) {
            sessionId = SnowflakeUtils.nextId(BizCodeEnum.SESSION);
            ChatSessionDto dto = new ChatSessionDto();
            dto.setTitle(userMessage.length() > 20 ? userMessage.substring(0, 20) + "..." : userMessage);
            chatSessionService.add(sessionId, dto);
        }
        // 保存用户消息并返回
        return saveMessage(sessionId, "user", userMessage);
    }

    @Override
    public SseEmitter chat(Long sessionId) {
        SseEmitter emitter = new SseEmitter(300000L);

        final Long finalSessionId = sessionId;
        // 获取历史消息
        List<ChatMessageVo> history = chatMessageService.listBySessionId(finalSessionId);
        executor.execute(() -> {
            StringBuilder fullResponse = new StringBuilder();
            try {
                // 调用 Python 服务
                URL url = new URL(pythonUrl + "/chat");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                // 构建请求体
                String requestBody = buildRequestBody(finalSessionId, history);
                conn.getOutputStream().write(requestBody.getBytes(StandardCharsets.UTF_8));

                // 读取 SSE 流
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("data:")) {
                            String data = line.substring(5).trim();
                            if ("[DONE]".equals(data)) {
                                emitter.send(SseEmitter.event().data("[DONE]"));
                                break;
                            }
                            fullResponse.append(data);
                            emitter.send(SseEmitter.event().data(data));
                        }
                    }
                }

                // 保存 AI 回复
                saveMessage(finalSessionId, "assistant", fullResponse.toString());
                emitter.complete();

            } catch (Exception e) {
                log.error("AI对话异常", e);
                try {
                    emitter.send(SseEmitter.event().data("error: " + e.getMessage()));
                } catch (Exception ex) {
                    log.error("发送错误消息失败", ex);
                }
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    //    保存消息
    private ChatMessageVo saveMessage(Long sessionId, String role, String content) {
        ChatMessage msg = new ChatMessage();
        msg.setSessionId(sessionId);
        msg.setRole(role);
        msg.setContent(content);
        msg.setCreateTime(LocalDateTime.now());
        chatMessageService.save(msg);

        ChatMessageVo vo = new ChatMessageVo();
        vo.setId(msg.getId());
        vo.setSessionId(sessionId);
        vo.setRole(role);
        vo.setContent(content);
        vo.setCreateTime(LocalDateTimeUtil.toEpochMilli(msg.getCreateTime()));
        return vo;
    }

    private String buildRequestBody(Long sessionId, List<ChatMessageVo> history) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"session_id\":").append(sessionId).append(",\"messages\":[");
        for (int i = 0; i < history.size(); i++) {
            ChatMessageVo msg = history.get(i);
            if (i > 0) sb.append(",");
            sb.append("{\"role\":\"").append(msg.getRole())
              .append("\",\"content\":\"").append(escapeJson(msg.getContent())).append("\"}");
        }
        sb.append("]}");
        return sb.toString();
    }

    private String escapeJson(String str) {
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}
