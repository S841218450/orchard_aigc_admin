package com.example.orchardai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "作品收录为素材DTO")
public class AiAssetCreateDto {

    @NotNull(message = "来源作品ID不能为空")
    @Schema(description = "来源作品ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long workId;

    @Schema(description = "标签列表")
    private List<String> tags;
}
