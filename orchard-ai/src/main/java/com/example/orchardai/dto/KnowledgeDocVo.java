package com.example.orchardai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "知识库文档VO")
public class KnowledgeDocVo {

    @Schema(description = "文档ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "所属目录ID（0表示根目录）")
    private Long folderId;

    @Schema(description = "文件名")
    private String fileName;

    @Schema(description = "COS文件URL")
    private String fileUrl;

    @Schema(description = "文件大小（字节）")
    private Long fileSize;

    @Schema(description = "文档类型")
    private String docType;

    @Schema(description = "状态：0-待处理 1-向量化中 2-已完成 3-失败")
    private Integer status;

    @Schema(description = "分块数量")
    private Integer chunkCount;

    @Schema(description = "token数量")
    private Long tokenCount;

    @Schema(description = "失败原因")
    private String errorMsg;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
