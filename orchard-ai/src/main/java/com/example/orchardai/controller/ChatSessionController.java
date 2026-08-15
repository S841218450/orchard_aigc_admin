package com.example.orchardai.controller;

import com.example.orchardai.client.AgentApiClient;
import com.example.orchardai.dto.ChatSessionDto;
import com.example.orchardai.dto.ChatSessionIdDto;
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
    private final AgentApiClient agentApiClient;

    @Operation(summary = "创建会话")
    @PostMapping("/add")
    public Result<ChatSessionVo> add(@Valid @RequestBody ChatSessionDto dto) {
        Long sessionId = SnowflakeUtils.nextId(BizCodeEnum.SESSION);
        return Result.ok(chatSessionService.add(sessionId, dto));
    }

    @Operation(summary = "更新会话")
    @PostMapping("/update")
    public Result<Void> update(@Valid @RequestBody ChatSessionDto dto) {
        chatSessionService.update(dto.getId(), dto);
        return Result.ok();
    }

    @Operation(summary = "删除会话")
    @PostMapping("/delete")
    public Result<Void> delete(@Valid @RequestBody ChatSessionIdDto dto) {
        // 先通知 Agent 清除会话记忆，成功后本地才删除，保证 Agent 记忆不残留
        agentApiClient.clearMemory(dto.getId().toString());
        chatSessionService.removeById(dto.getId());
        return Result.ok();
    }

    @Operation(summary = "获取用户会话列表")
    @GetMapping("/list")
    public Result<List<ChatSessionVo>> listByUserId(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(chatSessionService.listByUserId(userId));
    }
}
