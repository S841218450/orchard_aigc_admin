package com.example.orchardfile.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 文件上传响应
 */
@Data
@Schema(description = "文件上传响应")
public class FileUploadVo {

    @Schema(description = "文件ID")
    private Long fileId;

    @Schema(description = "文件名")
    private String fileName;

    @Schema(description = "原始文件名")
    private String originalName;

    @Schema(description = "文件大小（字节）")
    private Long fileSize;

    @Schema(description = "文件访问URL")
    private String fileUrl;

    @Schema(description = "文件类型")
    private String fileType;
}
