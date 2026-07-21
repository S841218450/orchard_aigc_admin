package com.example.orchardai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "AI请求DTO")
public class AiRequestDto {

    @Schema(description = "会话ID")
    private Long sessionId;

    @Schema(description = "用户输入")
    private String message;
}
