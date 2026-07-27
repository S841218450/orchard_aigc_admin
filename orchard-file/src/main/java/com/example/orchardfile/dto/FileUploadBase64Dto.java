package com.example.orchardfile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Base64文件上传DTO")
public class FileUploadBase64Dto {

    @Schema(description = "Base64编码的文件内容")
    private String base64;

    @Schema(description = "文件名（含扩展名）")
    private String fileName;

    @Schema(description = "用户ID（未登录时必填）")
    private Long userId;

    @Schema(description = "文件夹ID（可选）")
    private Long folderId;
}
