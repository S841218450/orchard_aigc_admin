package com.example.orchardai.controller;

import com.example.orchardai.dto.ChatMessageVo;
import com.example.orchardai.service.AiChatService;
import com.example.orchardcommon.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "AI对话")
@RestController
@RequestMapping("/api/ai/chat")
@RequiredArgsConstructor
public class AiChatController {

    private final AiChatService aiChatService;

    @Operation(summary = "发送用户消息")
    @PostMapping("/send")
    public Result<ChatMessageVo> send(@RequestParam(required = false) Long sessionId, @RequestParam String message) {
        return Result.ok(aiChatService.send(sessionId, message));
    }

    @Operation(summary = "AI对话（SSE流式输出）")
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@RequestParam Long sessionId) {
        return aiChatService.chat(sessionId);
    }
}
