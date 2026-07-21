package com.example.orchardfile.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.orchardcommon.entity.baseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文件记录实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_file_record")
@Schema(description = "文件记录")
public class FileRecord extends baseEntity {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "文件名")
    private String fileName;

    @Schema(description = "原始文件名")
    private String originalName;

    @Schema(description = "文件类型（后缀）")
    private String fileType;

    @Schema(description = "MIME类型")
    private String mimeType;

    @Schema(description = "文件大小（字节）")
    private Long fileSize;

    @Schema(description = "COS存储路径")
    private String cosPath;

    @Schema(description = "文件访问URL")
    private String fileUrl;

    @Schema(description = "所属文件夹ID（null表示根目录）")
    private Long folderId;

    @Schema(description = "状态：0-删除 1-正常")
    private Integer status;
}
