package com.example.orchardai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "AI素材ID请求")
public class AiAssetIdDto {

    @NotNull(message = "素材ID不能为空")
    @Schema(description = "素材ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;
}
