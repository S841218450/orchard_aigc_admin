package com.example.orchardfile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "创建文件夹请求")
public class FolderCreateDto {

    @NotBlank(message = "文件夹名称不能为空")
    @Schema(description = "文件夹名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String folderName;

    @Schema(description = "上级文件夹ID（可选，不传则在根目录）")
    private Long parentId;
}
