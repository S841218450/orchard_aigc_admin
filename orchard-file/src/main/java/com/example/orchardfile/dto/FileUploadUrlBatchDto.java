package com.example.orchardfile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "URL批量文件上传DTO")
public class FileUploadUrlBatchDto {

    @Schema(description = "文件列表")
    private List<FileUploadUrlDto> files;

    @Schema(description = "用户ID（全局默认值，优先级低于每个文件自身的userId）")
    private Long userId;

    @Schema(description = "文件夹ID（全局默认值，传了（含0根目录）则写入文件系统；不传则每个文件自己的folderId决定）")
    private Long folderId;
}
