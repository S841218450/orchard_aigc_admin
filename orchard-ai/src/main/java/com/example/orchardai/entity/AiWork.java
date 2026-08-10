package com.example.orchardai.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.orchardcommon.entity.baseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_ai_work")
@Schema(description = "AI作品")
public class AiWork extends baseEntity {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "作品类型：image-文生图/图生图 video-文生视频")
    private String type;

    @Schema(description = "提示词")
    private String prompt;

    @Schema(description = "模型标识")
    private String model;

    @Schema(description = "参数JSON")
    private String params;

    @Schema(description = "结果URL（当前展示图）")
    private String resultUrl;

    @Schema(description = "结果数据列表JSON（多图场景，元素为{id,url}）")
    private String dataList;

    @Schema(description = "原图数据列表JSON（图生图参考图，元素为{id,url}）")
    private String originImageList;

    @Schema(description = "状态：0-等待中 1-生成中 2-已完成 3-失败 4-待操作")
    private Integer status;

    @Schema(description = "待操作数据JSON（如选择列表）")
    private String operationData;
}
