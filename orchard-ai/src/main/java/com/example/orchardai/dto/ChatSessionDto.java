package com.example.orchardai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "会话DTO")
public class ChatSessionDto {

    @Schema(description = "主键ID（更新/删除时必填）")
    private Long id;

    @NotBlank(message = "会话标题不能为空")
    @Schema(description = "会话标题")
    private String title;
}
