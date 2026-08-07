package com.example.orchardfile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Base64批量文件上传DTO")
public class FileUploadBase64BatchDto {

    @NotEmpty(message = "文件列表不能为空")
    @Valid
    @Schema(description = "文件列表")
    private List<FileUploadBase64Dto> files;

    @Schema(description = "用户ID（全局默认值，优先级低于每个文件自身的userId）")
    private Long userId;

    @Schema(description = "文件夹ID（全局默认值，传了（含0根目录）则写入文件系统；不传则每个文件自己的folderId决定）")
    private Long folderId;
}
