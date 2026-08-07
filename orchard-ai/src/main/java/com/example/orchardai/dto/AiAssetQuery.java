package com.example.orchardai.dto;

import com.example.orchardcommon.entity.BaseQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "AI素材查询参数")
public class AiAssetQuery extends BaseQuery {

    @Schema(description = "素材类型：image/video")
    private String type;

    @Schema(description = "标签精确匹配")
    private String tag;
}
