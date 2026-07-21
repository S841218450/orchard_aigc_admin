package com.example.orchardauth.service;

import com.example.orchardauth.vo.LoginVo;
import com.example.orchardauth.vo.OAuthBinddingVo;

import java.util.List;

public interface OAuthService {

    /**
     * 获取第三方登录授权URL
     */
    String getAuthorizationUrl(String oauthType);

    /**
     * 第三方登录回调
     */
    LoginVo callback(String oauthType, String code, String state);

    /**
     * 绑定手机号
     */
    void bindPhone(Long userId, String phone, String code);

    /**
     * 绑定第三方平台
     */
    void bindOAuth(Long userId, String oauthType, String code, String state);

    /**
     * 解绑第三方平台
     */
    void unbindOAuth(Long userId, String oauthType);

    /**
     * 获取用户绑定的第三方平台列表
     */
    List<OAuthBinddingVo> getBinddings(Long userId);
}
