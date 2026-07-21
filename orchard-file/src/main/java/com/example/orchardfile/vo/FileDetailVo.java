package com.example.orchardfile.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文件详情响应
 */
@Data
@Schema(description = "文件详情响应")
public class FileDetailVo {

    @Schema(description = "文件ID")
    private Long id;

    @Schema(description = "文件名")
    private String fileName;

    @Schema(description = "原始文件名")
    private String originalName;

    @Schema(description = "文件类型（后缀）")
    private String fileType;

    @Schema(description = "文件大小（字节）")
    private Long fileSize;

    @Schema(description = "文件访问URL")
    private String fileUrl;

    @Schema(description = "所属文件夹ID")
    private Long folderId;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
