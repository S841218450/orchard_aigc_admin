package com.example.orchardai.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.orchardcommon.entity.baseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_ai_asset")
@Schema(description = "AI素材")
public class AiAsset extends baseEntity {

    @Schema(description = "作者用户ID")
    private Long userId;

    @Schema(description = "素材类型：image-图片 video-视频")
    private String type;

    @Schema(description = "提示词")
    private String prompt;

    @Schema(description = "参数JSON（模型/尺寸等）")
    private String params;

    @Schema(description = "素材URL")
    private String url;

    @Schema(description = "标签JSON数组")
    private String tags;

    @Schema(description = "点赞数")
    private Integer likeCount;
}
