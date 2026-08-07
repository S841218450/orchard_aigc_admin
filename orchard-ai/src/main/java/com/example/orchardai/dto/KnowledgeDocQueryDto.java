package com.example.orchardai.dto;

import com.example.orchardcommon.entity.BaseQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "知识库文档查询请求")
public class KnowledgeDocQueryDto extends BaseQuery {

    @Schema(description = "所属目录ID：0=根目录；不传=查全部；具体值=查该目录下")
    private Long folderId;

    @Schema(description = "状态筛选（可选）")
    private Integer status;
}
