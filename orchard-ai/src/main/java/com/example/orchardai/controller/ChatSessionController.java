package com.example.orchardai.controller;

import com.example.orchardai.dto.ChatSessionDto;
import com.example.orchardai.dto.ChatSessionVo;
import com.example.orchardai.service.ChatSessionService;
import com.example.orchardcommon.business.SnowflakeId.BizCodeEnum;
import com.example.orchardcommon.business.SnowflakeId.SnowflakeUtils;
import com.example.orchardcommon.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "会话管理")
@RestController
@RequestMapping("/api/ai/session")
@RequiredArgsConstructor
public class ChatSessionController {

    private final ChatSessionService chatSessionService;

    @Operation(summary = "创建会话")
    @PostMapping("/add")
    public Result<Void> add(@Valid @RequestBody ChatSessionDto dto) {
        Long sessionId = SnowflakeUtils.nextId(BizCodeEnum.SESSION);
        chatSessionService.add(sessionId, dto);
        return Result.ok();
    }

    @Operation(summary = "更新会话")
    @PostMapping("/update/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ChatSessionDto dto) {
        chatSessionService.update(id, dto);
        return Result.ok();
    }

    @Operation(summary = "删除会话")
    @PostMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        chatSessionService.removeById(id);
        return Result.ok();
    }

    @Operation(summary = "获取用户会话列表")
    @GetMapping("/list")
    public Result<List<ChatSessionVo>> listByUserId(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(chatSessionService.listByUserId(userId));
    }
}
