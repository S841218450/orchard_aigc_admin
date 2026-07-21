package com.example.orchardauth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 第三方绑定信息
 */
@Data
@Schema(description = "第三方绑定信息")
public class OAuthBinddingVo {

    @Schema(description = "第三方平台：wechat/alipay/github")
    private String oauthType;

    @Schema(description = "平台昵称")
    private String nickname;

    @Schema(description = "平台头像")
    private String avatar;

    @Schema(description = "绑定时间")
    private String createTime;
}
