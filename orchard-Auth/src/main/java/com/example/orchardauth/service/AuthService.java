package com.example.orchardauth.service;

import com.example.orchardauth.dto.*;
import com.example.orchardauth.vo.LoginVo;
import com.example.orchardauth.vo.UserInfoVo;

public interface AuthService {

    /**
     * 手机号+密码登录
     */
    LoginVo loginByPassword(LoginByPasswordDto dto);

    /**
     * 手机号+验证码登录
     */
    LoginVo loginBySms(LoginBySmsDto dto);

    /**
     * 注册
     */
    void register(RegisterDto dto);

    /**
     * 刷新Token
     */
    LoginVo refreshToken(RefreshTokenDto dto);

    /**
     * 退出登录
     */
    void logout(String accessToken);

    /**
     * 获取当前用户信息
     */
    UserInfoVo getUserInfo(Long userId);

    /**
     * 绑定手机号
     */
    void bindPhone(Long userId, BindPhoneDto dto);
}
