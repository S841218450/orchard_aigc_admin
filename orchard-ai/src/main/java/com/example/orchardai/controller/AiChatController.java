package com.example.orchardai.controller;

import com.example.orchardai.dto.ChatMessageVo;
import com.example.orchardai.dto.ChatSendRequest;
import com.example.orchardai.service.AiChatService;
import com.example.orchardcommon.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "AI对话")
@RestController
@RequestMapping("/api/ai/chat")
@RequiredArgsConstructor
public class AiChatController {

    private final AiChatService aiChatService;

    @Operation(summary = "发送用户消息")
    @PostMapping("/send")
    public Result<ChatMessageVo> send(@RequestBody ChatSendRequest request) {
        return Result.ok(aiChatService.send(request));
    }

    @Operation(summary = "删除消息")
    @DeleteMapping("/delete/{messageId}")
    public Result<Void> delete(@PathVariable Long messageId) {
        aiChatService.delete(messageId);
        return Result.ok();
    }
}
