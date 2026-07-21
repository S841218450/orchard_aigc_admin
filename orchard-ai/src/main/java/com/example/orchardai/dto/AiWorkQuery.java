package com.example.orchardai.dto;

import com.example.orchardcommon.entity.BaseQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "AI作品查询参数")
public class AiWorkQuery extends BaseQuery {

    @Schema(description = "作品类型：image/video")
    private String type;
}
