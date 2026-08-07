package com.example.orchardfile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "URL文件上传DTO")
public class FileUploadUrlDto {

    @Schema(description = "文件URL")
    private String url;

    @Schema(description = "文件名（可选，不带扩展名会自动从URL解析）")
    private String fileName;

    @Schema(description = "用户ID（未登录时必填，仅 folderId 非空时生效）")
    private Long userId;

    @Schema(description = "文件夹ID：传了（含0根目录）则写入文件系统file_record；不传则只上传COS返回URL")
    private Long folderId;
}
