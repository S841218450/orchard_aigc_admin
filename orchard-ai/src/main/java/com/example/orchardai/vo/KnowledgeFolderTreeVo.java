package com.example.orchardai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "知识库目录树节点")
public class KnowledgeFolderTreeVo {

    @Schema(description = "目录ID")
    private Long id;

    @Schema(description = "目录名称")
    private String folderName;

    @Schema(description = "父目录ID")
    private Long parentId;

    @Schema(description = "目录下文档数量")
    private Long docCount;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "子目录")
    private List<KnowledgeFolderTreeVo> children;
}
