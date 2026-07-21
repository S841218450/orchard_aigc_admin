package com.example.orchardai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "消息DTO")
public class ChatMessageDto {

    @NotNull(message = "会话ID不能为空")
    @Schema(description = "会话ID")
    private Long sessionId;

    @NotBlank(message = "消息内容不能为空")
    @Schema(description = "消息内容")
    private String content;
}
