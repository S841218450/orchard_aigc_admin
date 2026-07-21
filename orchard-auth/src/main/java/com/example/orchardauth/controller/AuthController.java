package com.example.orchardauth.controller;

import com.example.orchardauth.dto.*;
import com.example.orchardauth.service.AuthService;
import com.example.orchardauth.service.OAuthService;
import com.example.orchardauth.service.SmsCodeService;
import com.example.orchardauth.vo.LoginVo;
import com.example.orchardauth.vo.OAuthBinddingVo;
import com.example.orchardauth.vo.UserInfoVo;
import com.example.orchardcommon.annotation.LogOperation;
import com.example.orchardcommon.annotation.PublicApi;
import com.example.orchardcommon.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "认证管理")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    @Autowired
    private AuthService authService;
    @Autowired
    private OAuthService oAuthService;
    @Autowired
    private SmsCodeService smsCodeService;

    @Operation(summary = "发送验证码")
    @PublicApi
    @LogOperation(module = "认证", type = "发送验证码", description = "发送手机验证码")
    @PostMapping("/sms/send")
    public Result<Void> sendSmsCode(@RequestParam String phone) {
        smsCodeService.sendCode(phone);
        return Result.ok();
    }

    @Operation(summary = "手机号+密码登录")
    @PublicApi
    @LogOperation(module = "认证", type = "登录", description = "手机号密码登录")
    @PostMapping("/login/password")
    public Result<LoginVo> loginByPassword(@Valid @RequestBody LoginByPasswordDto dto) {
        LoginVo vo = authService.loginByPassword(dto);
        return Result.ok(vo);
    }

    @Operation(summary = "手机号+验证码登录")
    @PublicApi
    @LogOperation(module = "认证", type = "登录", description = "手机号验证码登录")
    @PostMapping("/login/sms")
    public Result<LoginVo> loginBySms(@Valid @RequestBody LoginBySmsDto dto) {
        LoginVo vo = authService.loginBySms(dto);
        return Result.ok(vo);
    }

    @Operation(summary = "注册")
    @PublicApi
    @LogOperation(module = "认证", type = "注册", description = "用户注册")
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDto dto) {
        authService.register(dto);
        return Result.ok();
    }

    @Operation(summary = "刷新Token")
    @PublicApi
    @LogOperation(module = "认证", type = "刷新Token", description = "刷新访问令牌")
    @PostMapping("/refresh")
    public Result<LoginVo> refreshToken(@Valid @RequestBody RefreshTokenDto dto) {
        LoginVo vo = authService.refreshToken(dto);
        return Result.ok(vo);
    }

    @Operation(summary = "退出登录")
    @LogOperation(module = "认证", type = "退出", description = "用户退出登录")
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        authService.logout(token);
        return Result.ok();
    }

    @Operation(summary = "获取当前用户信息")
    @LogOperation(module = "用户", type = "查询", description = "获取当前用户信息")
    @GetMapping("/user/info")
    public Result<UserInfoVo> getUserInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        UserInfoVo vo = authService.getUserInfo(userId);
        return Result.ok(vo);
    }

    @Operation(summary = "获取第三方登录授权URL")
    @PublicApi
    @LogOperation(module = "认证", type = "OAuth", description = "获取第三方登录授权URL")
    @GetMapping("/oauth/url")
    public Result<String> getOAuthUrl(@RequestParam String oauthType) {
        String url = oAuthService.getAuthorizationUrl(oauthType);
        return Result.ok(url);
    }

    @Operation(summary = "第三方登录回调")
    @PublicApi
    @LogOperation(module = "认证", type = "OAuth", description = "第三方登录回调")
    @GetMapping("/oauth/callback")
    public Result<LoginVo> oauthCallback(@RequestParam(required = false) String oauthType,
                                         @RequestParam String code,
                                         @RequestParam(required = false) String state) {
        LoginVo vo = oAuthService.callback(oauthType, code, state);
        return Result.ok(vo);
    }

    @Operation(summary = "绑定手机号")
    @LogOperation(module = "认证", type = "绑定", description = "绑定手机号")
    @PostMapping("/bind/phone")
    public Result<Void> bindPhone(@Valid @RequestBody BindPhoneDto dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        authService.bindPhone(userId, dto);
        return Result.ok();
    }

    @Operation(summary = "绑定第三方平台")
    @LogOperation(module = "认证", type = "绑定", description = "绑定第三方平台")
    @GetMapping("/bind/oauth")
    public Result<Void> bindOAuth(@RequestParam String oauthType, @RequestParam String code,
                                   @RequestParam(required = false) String state, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        oAuthService.bindOAuth(userId, oauthType, code, state);
        return Result.ok();
    }

    @Operation(summary = "解绑第三方平台")
    @LogOperation(module = "认证", type = "解绑", description = "解绑第三方平台")
    @DeleteMapping("/bind/oauth/{oauthType}")
    public Result<Void> unbindOAuth(@PathVariable String oauthType, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        oAuthService.unbindOAuth(userId, oauthType);
        return Result.ok();
    }

    @Operation(summary = "获取绑定的第三方平台列表")
    @LogOperation(module = "认证", type = "查询", description = "获取绑定的第三方平台列表")
    @GetMapping("/bind/oauth/list")
    public Result<List<OAuthBinddingVo>> getBinddings(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<OAuthBinddingVo> list = oAuthService.getBinddings(userId);
        return Result.ok(list);
    }
}
