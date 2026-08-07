package com.example.orchardai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "创建知识库目录请求")
public class KnowledgeFolderCreateDto {

    @NotBlank(message = "目录名称不能为空")
    @Schema(description = "目录名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String folderName;

    @Schema(description = "上级目录ID（可选，不传或传0表示在根目录下创建）")
    private Long parentId;
}
