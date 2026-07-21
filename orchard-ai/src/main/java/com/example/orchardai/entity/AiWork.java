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

    @Schema(description = "作品类型：image-文生图 video-文生视频")
    private String type;

    @Schema(description = "提示词")
    private String prompt;

    @Schema(description = "模型标识")
    private String model;

    @Schema(description = "参数JSON")
    private String params;

    @Schema(description = "结果URL")
    private String resultUrl;

    @Schema(description = "状态：0-等待中 1-生成中 2-已完成 3-失败 4-待操作")
    private Integer status;
}
