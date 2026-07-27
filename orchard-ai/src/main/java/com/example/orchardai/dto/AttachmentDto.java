package com.example.orchardai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "附件信息")
public class AttachmentDto {

    @Schema(description = "附件URL")
    private String url;

    @Schema(description = "文件名")
    private String fileName;

    @Schema(description = "附件类型：image-图片 file-文件")
    private String type;
}