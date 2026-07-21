package com.example.orchardai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "AI响应DTO")
public class AiResponseDto {

    @Schema(description = "AI回复内容")
    private String content;

    @Schema(description = "是否完成")
    private Boolean done;
}
