package com.example.orchardai.controller;

import com.example.orchardai.dto.ChatMessageUpdateDto;
import com.example.orchardai.dto.ChatMessageVo;
import com.example.orchardai.dto.ChatSendRequest;
import com.example.orchardai.service.AiChatService;
import com.example.orchardcommon.annotation.InternalApi;
import com.example.orchardcommon.dto.IdDto;
import com.example.orchardcommon.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

    @InternalApi
    @Operation(summary = "更新消息（Agent生成完回答回调，回写回答与状态）")
    @PutMapping("/update")
    public Result<Void> update(@Valid @RequestBody ChatMessageUpdateDto dto) {
        aiChatService.update(dto);
        return Result.ok();
    }

    @Operation(summary = "删除消息（整段删除，提问+回答）")
    @PostMapping("/delete")
    public Result<Void> delete(@RequestBody IdDto dto) {
        aiChatService.delete(dto.getId());
        return Result.ok();
    }
}
