package com.example.orchardfile.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文件目录树节点
 */
@Data
@Schema(description = "文件目录树节点")
public class FolderTreeVo {

    @Schema(description = "目录ID")
    private Long id;

    @Schema(description = "目录名称")
    private String folderName;

    @Schema(description = "父目录ID")
    private Long parentId;

    @Schema(description = "排序号")
    private Integer sort;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "子目录")
    private List<FolderTreeVo> children;
}
