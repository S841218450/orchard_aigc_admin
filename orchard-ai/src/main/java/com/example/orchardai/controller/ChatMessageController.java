package com.example.orchardai.controller;

import com.example.orchardai.dto.ChatMessageDto;
import com.example.orchardai.dto.ChatMessageVo;
import com.example.orchardai.service.ChatMessageService;
import com.example.orchardcommon.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "消息管理")
@RestController
@RequestMapping("/api/ai/message")
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatMessageService chatMessageService;

    @Operation(summary = "获取会话消息列表")
    @GetMapping("/list")
    public Result<List<ChatMessageVo>> listBySessionId(@RequestParam Long sessionId) {
        return Result.ok(chatMessageService.listBySessionId(sessionId));
    }
    @Operation(summary = "删除消息")
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        chatMessageService.removeById(id);
        return Result.ok();
    }
}
