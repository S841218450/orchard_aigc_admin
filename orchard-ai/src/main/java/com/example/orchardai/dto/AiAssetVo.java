package com.example.orchardai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "AI素材VO")
public class AiAssetVo {

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

    @Schema(description = "标签列表")
    private List<String> tags;

    @Schema(description = "点赞数")
    private Integer likeCount;

    @Schema(description = "当前用户是否已点赞（未登录为false）")
    private Boolean liked;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
