package com.example.orchardfile.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.orchardcommon.entity.baseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文件目录实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_file_folder")
@Schema(description = "文件目录")
public class FileFolder extends baseEntity {

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
}
