package com.example.orchardauth.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.example.orchardcommon.entity.baseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 第三方登录绑定实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_oauth_bindding")
public class OAuthBindding extends baseEntity {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 第三方平台：wechat-微信 alipay-支付宝 github-GitHub
     */
    private String oauthType;

    /**
     * 第三方平台OpenID
     */
    private String openId;

    /**
     * 第三方平台UnionID
     */
    private String unionId;

    /**
     * 第三方昵称
     */
    private String nickname;

    /**
     * 第三方头像
     */
    private String avatar;
}
