package com.example.orchardai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "会话idDTO")
public class ChatSessionIdDto {
    @Schema(description = "主键ID（更新/删除时必填）")
    private Long id;
}
