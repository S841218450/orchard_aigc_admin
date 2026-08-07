package com.example.orchardai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI素材 SQL 联查中间对象（JOIN t_user 取作者昵称，tags 为原始 JSON 字符串）
 */
@Data
@Schema(description = "AI素材联查行")
public class AiAssetRow {

    @Schema(description = "素材ID")
    private Long id;

    @Schema(description = "作者用户ID")
    private Long userId;

    @Schema(description = "作者昵称")
    private String authorName;

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

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
