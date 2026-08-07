package com.example.orchardai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("knowledge_folder")
@Schema(description = "知识库目录")
public class KnowledgeFolder {

    @TableId(type = IdType.INPUT)
    @Schema(description = "目录ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "目录名称")
    private String folderName;

    @Schema(description = "父目录ID（null表示根目录）")
    private Long parentId;

    @Schema(description = "排序号")
    private Integer sort;

    @Schema(description = "状态：0-删除 1-正常")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
