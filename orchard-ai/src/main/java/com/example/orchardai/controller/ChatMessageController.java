package com.example.orchardai.controller;

import com.example.orchardai.dto.ChatMessageQuery;
import com.example.orchardai.dto.ChatMessageVo;
import com.example.orchardai.service.ChatMessageService;
import com.example.orchardcommon.result.PageResult;
import com.example.orchardcommon.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "消息管理")
@RestController
@RequestMapping("/api/ai/message")
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatMessageService chatMessageService;

    @Operation(summary = "获取会话消息列表（分页）")
    @PostMapping("/page")
    public Result<PageResult<ChatMessageVo>> page(@RequestBody ChatMessageQuery dto) {
        return Result.ok(chatMessageService.pageBySessionId(dto));
    }
    @Operation(summary = "删除消息")
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        chatMessageService.removeById(id);
        return Result.ok();
    }
}
