package com.example.orchardai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "AI作品ID请求")
public class AiWorkIdDto {

    @NotNull(message = "作品ID不能为空")
    @Schema(description = "作品ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;
}
