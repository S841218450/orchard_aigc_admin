package com.example.orchardai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("knowledge_doc")
@Schema(description = "知识库文档")
public class KnowledgeDoc {

    @TableId(type = IdType.INPUT)
    @Schema(description = "文档ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "所属目录ID（null/0表示根目录）")
    private Long folderId;

    @Schema(description = "文件名")
    private String fileName;

    @Schema(description = "COS文件URL")
    private String fileUrl;

    @Schema(description = "文件大小（字节）")
    private Long fileSize;

    @Schema(description = "文档类型（txt/docx/pdf/md等）")
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

    @TableLogic
    @Schema(description = "是否删除")
    private Integer deleted;
}
