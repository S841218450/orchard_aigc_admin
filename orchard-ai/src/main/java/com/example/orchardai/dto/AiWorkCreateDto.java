package com.example.orchardai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
@Schema(description = "创建AI作品DTO")
public class AiWorkCreateDto {

    @NotBlank(message = "作品类型不能为空")
    @Schema(description = "作品类型：image-文生图/图生图 video-文生视频")
    private String type;

    @NotBlank(message = "提示词不能为空")
    @Schema(description = "提示词")
    private String prompt;

    @Schema(description = "模型标识，默认default")
    private String model;

    @Schema(description = "参数，如 style/imageProportion/imageQuality/imageCount")
    private Map<String, Object> params;
}
