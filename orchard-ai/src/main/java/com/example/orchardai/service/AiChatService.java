package com.example.orchardai.service;

import com.example.orchardai.dto.ChatMessageVo;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface AiChatService {

    ChatMessageVo send(Long sessionId, String userMessage);

    SseEmitter chat(Long sessionId);
}
