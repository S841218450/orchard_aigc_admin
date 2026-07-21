package com.example.orchardauth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户信息响应
 */
@Data
@Schema(description = "用户信息响应")
public class UserInfoVo {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "状态：0-禁用 1-正常")
    private Integer status;
}
