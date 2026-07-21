package com.example.orchardai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "消息VO")
public class ChatMessageVo {

    @Schema(description = "消息ID")
    private Long id;

    @Schema(description = "会话ID")
    private Long sessionId;

    @Schema(description = "角色：user-用户 assistant-AI")
    private String role;

    @Schema(description = "消息内容")
    private String content;

    @Schema(description = "创建时间")
    private Long createTime;
}
