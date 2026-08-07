package com.example.orchardai.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.orchardcommon.entity.baseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_ai_asset_like")
@Schema(description = "AI素材点赞")
public class AiAssetLike extends baseEntity {

    @Schema(description = "素材ID")
    private Long assetId;

    @Schema(description = "用户ID")
    private Long userId;
}
