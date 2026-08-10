package com.example.orchardai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "图片项：原图与生成图通用结构")
public class ImageItem {

    @Schema(description = "图片ID（文件ID）")
    private Long id;

    @Schema(description = "图片URL")
    private String url;
}
