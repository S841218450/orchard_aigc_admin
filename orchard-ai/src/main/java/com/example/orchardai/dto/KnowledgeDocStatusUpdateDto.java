package com.example.orchardai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "知识库文档状态更新请求")
public class KnowledgeDocStatusUpdateDto {

    @NotNull(message = "文档ID不能为空")
    @Schema(description = "文档ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @NotNull(message = "目标状态不能为空")
    @Schema(description = "目标状态：0-待处理 1-向量化中 2-已完成 3-失败", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer status;

    @Schema(description = "分块数量（完成时传）")
    private Integer chunkCount;

    @Schema(description = "token数量（完成时传）")
    private Long tokenCount;

    @Schema(description = "文件大小（字节，可选，用于复用自有COS文件时回填）")
    private Long fileSize;

    @Schema(description = "失败原因（失败时必填）")
    private String errorMsg;
}
