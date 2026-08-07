package com.example.orchardfile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "文件上传业务参数")
public class FileUploadDto {

    @Schema(description = "用户ID（未登录时通过参数传递，仅 folderId 非空时生效）")
    private Long userId;

    @Schema(description = "文件夹ID：传了（含0根目录）则写入文件系统file_record；不传则只上传COS返回URL")
    private Long folderId;
}
