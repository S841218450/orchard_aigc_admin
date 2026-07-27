package com.example.orchardai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Schema(description = "AI作品VO")
public class AiWorkVo {

    @Schema(description = "作品ID")
    private Long id;

    @Schema(description = "作品类型：image-文生图 video-文生视频")
    private String type;

    @Schema(description = "提示词")
    private String prompt;

    @Schema(description = "模型标识")
    private String model;

    @Schema(description = "参数")
    private Map<String, Object> params;

    @Schema(description = "结果URL（当前展示图）")
    private String resultUrl;

    @Schema(description = "结果数据列表（多图场景）")
    private List<String> dataList;

    @Schema(description = "状态：0-待生成 1-生成中 2-已完成 3-失败 4-待操作")
    private Integer status;

    @Schema(description = "待操作数据（如选择列表）")
    private Object operationData;

    @Schema(description = "创建时间（时间戳）")
    private Long createTime;
}
