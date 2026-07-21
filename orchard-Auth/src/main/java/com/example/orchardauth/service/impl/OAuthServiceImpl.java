package com.example.orchardauth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.orchardauth.entity.OAuthBindding;
import com.example.orchardauth.mapper.OAuthBinddingMapper;
import com.example.orchardauth.service.OAuthService;
import com.example.orchardauth.service.SmsCodeService;
import com.example.orchardauth.util.JwtUtil;
import com.example.orchardauth.vo.LoginVo;
import com.example.orchardauth.vo.OAuthBinddingVo;
import com.example.orchardcommon.business.SnowflakeId.BizCodeEnum;
import com.example.orchardcommon.business.SnowflakeId.SnowflakeUtils;
import com.example.orchardusermanagement.entity.User;
import com.example.orchardusermanagement.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.AuthRequest;
import me.zhyd.oauth.request.AuthWeChatOpenRequest;
import me.zhyd.oauth.request.AuthAlipayRequest;
import me.zhyd.oauth.request.AuthGithubRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthServiceImpl implements OAuthService {

    private final UserMapper userMapper;
    private final OAuthBinddingMapper oauthBinddingMapper;
    private final JwtUtil jwtUtil;
    private final SmsCodeService smsCodeService;

    @Value("${oauth.redirect-uri:http://localhost:8080/auth/oauth/callback}")
    private String redirectUri;

    @Value("${oauth.wechat.app-id:}")
    private String wechatAppId;

    @Value("${oauth.wechat.app-secret:}")
    private String wechatAppSecret;

    @Value("${oauth.alipay.app-id:}")
    private String alipayAppId;

    @Value("${oauth.alipay.alipay-public-key:}")
    private String alipayPublicKey;

    @Value("${oauth.alipay.private-key:}")
    private String alipayPrivateKey;

    @Value("${oauth.github.client-id:}")
    private String githubClientId;

    @Value("${oauth.github.client-secret:}")
    private String githubClientSecret;

    @Override
    public String getAuthorizationUrl(String oauthType) {
        AuthRequest authRequest = getAuthRequest(oauthType);
        // 将 oauthType 编码到 state 参数中，回调时用于识别来源
        return authRequest.authorize(oauthType + "_oauth_state");
    }

    @Override
    public LoginVo callback(String oauthType, String code, String state) {
        // 从 state 参数中提取真实的 oauthType（如果存在）
        if (state != null && state.endsWith("_oauth_state")) {
            oauthType = state.replace("_oauth_state", "");
        }
        
        AuthUser authUser = getAuthUser(oauthType, code, state);

        // 查找是否已绑定
        LambdaQueryWrapper<OAuthBindding> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OAuthBindding::getOauthType, oauthType)
               .eq(OAuthBindding::getOpenId, authUser.getUuid());
        OAuthBindding bindding = oauthBinddingMapper.selectOne(wrapper);

        User user;
        if (bindding != null) {
            // 已绑定，直接登录
            user = userMapper.selectById(bindding.getUserId());
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }
            // 更新第三方信息
            bindding.setNickname(authUser.getNickname());
            bindding.setAvatar(authUser.getAvatar());
            oauthBinddingMapper.updateById(bindding);
        } else {
            // 未绑定，创建新用户并绑定
            user = new User();
            user.setId(SnowflakeUtils.nextId(BizCodeEnum.USER));
            user.setNickname(authUser.getNickname());
            user.setAvatar(authUser.getAvatar());
            user.setStatus(1);
            user.setUserType(2); // 个人用户
            user.setCreateTime(LocalDateTime.now());
            userMapper.insert(user);

            // 创建绑定关系
            bindding = new OAuthBindding();
            Long userId = SnowflakeUtils.nextId(BizCodeEnum.USER);
            bindding.setId(userId);
            bindding.setUserId(user.getId());
            bindding.setOauthType(oauthType);
            bindding.setOpenId(authUser.getUuid());
            bindding.setUnionId(authUser.getToken().getUnionId());
            bindding.setNickname(authUser.getNickname());
            bindding.setAvatar(authUser.getAvatar());
            bindding.setCreateTime(LocalDateTime.now());
            bindding.setCreateBy(userId);
            oauthBinddingMapper.insert(bindding);

            log.info("第三方登录自动注册：userId={}, oauthType={}", user.getId(), oauthType);
        }

        // 检查状态
        if (user.getStatus() != 1) {
            throw new RuntimeException("账号已被禁用");
        }

        // 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);

        return buildLoginVo(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindPhone(Long userId, String phone, String code) {
        // 验证验证码
        smsCodeService.verifyCode(phone, code);

        // 检查手机号是否已被其他用户绑定
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, phone);
        User existUser = userMapper.selectOne(wrapper);
        if (existUser != null && !existUser.getId().equals(userId)) {
            throw new RuntimeException("该手机号已被其他账号绑定");
        }

        // 绑定手机号
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setPhone(phone);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        log.info("绑定手机号成功：userId={}, phone={}", userId, phone);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindOAuth(Long userId, String oauthType, String code, String state) {
        AuthUser authUser = getAuthUser(oauthType, code, state);

        // 检查该第三方账号是否已被绑定
        LambdaQueryWrapper<OAuthBindding> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OAuthBindding::getOauthType, oauthType)
               .eq(OAuthBindding::getOpenId, authUser.getUuid());
        OAuthBindding existBindding = oauthBinddingMapper.selectOne(wrapper);
        if (existBindding != null) {
            if (existBindding.getUserId().equals(userId)) {
                throw new RuntimeException("该" + getOauthTypeName(oauthType) + "账号已绑定到当前账号");
            }
            throw new RuntimeException("该" + getOauthTypeName(oauthType) + "账号已被其他账号绑定");
        }

        // 创建绑定关系
        OAuthBindding bindding = new OAuthBindding();
        bindding.setId(SnowflakeUtils.nextId(BizCodeEnum.USER));
        bindding.setUserId(userId);
        bindding.setOauthType(oauthType);
        bindding.setOpenId(authUser.getUuid());
        bindding.setUnionId(authUser.getToken().getUnionId());
        bindding.setNickname(authUser.getNickname());
        bindding.setAvatar(authUser.getAvatar());
        bindding.setCreateTime(LocalDateTime.now());
        oauthBinddingMapper.insert(bindding);

        log.info("绑定第三方平台成功：userId={}, oauthType={}", userId, oauthType);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unbindOAuth(Long userId, String oauthType) {
        // 检查是否已绑定
        LambdaQueryWrapper<OAuthBindding> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OAuthBindding::getUserId, userId)
               .eq(OAuthBindding::getOauthType, oauthType);
        OAuthBindding bindding = oauthBinddingMapper.selectOne(wrapper);
        if (bindding == null) {
            throw new RuntimeException("未绑定该第三方平台");
        }

        // 检查用户是否有手机号或其他绑定方式
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 如果没有手机号，检查是否还有其他第三方绑定
        if (user.getPhone() == null || user.getPhone().isEmpty()) {
            LambdaQueryWrapper<OAuthBindding> otherWrapper = new LambdaQueryWrapper<>();
            otherWrapper.eq(OAuthBindding::getUserId, userId)
                       .ne(OAuthBindding::getOauthType, oauthType);
            if (oauthBinddingMapper.selectCount(otherWrapper) == 0) {
                throw new RuntimeException("请先绑定手机号或其他第三方平台，否则将无法登录");
            }
        }

        // 解绑
        oauthBinddingMapper.deleteById(bindding.getId());
        log.info("解绑第三方平台成功：userId={}, oauthType={}", userId, oauthType);
    }

    @Override
    public List<OAuthBinddingVo> getBinddings(Long userId) {
        LambdaQueryWrapper<OAuthBindding> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OAuthBindding::getUserId, userId);
        List<OAuthBindding> binddings = oauthBinddingMapper.selectList(wrapper);

        return binddings.stream().map(b -> {
            OAuthBinddingVo vo = new OAuthBinddingVo();
            vo.setOauthType(b.getOauthType());
            vo.setNickname(b.getNickname());
            vo.setAvatar(b.getAvatar());
            vo.setCreateTime(b.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 获取第三方用户信息
     */
    private AuthUser getAuthUser(String oauthType, String code, String state) {
        AuthRequest authRequest = getAuthRequest(oauthType);
        
        AuthCallback authCallback = new AuthCallback();
        authCallback.setCode(code);
        authCallback.setState(state);

        AuthResponse<AuthUser> response = authRequest.login(authCallback);
        if (!response.ok()) {
            throw new RuntimeException("第三方登录失败：" + response.getMsg());
        }

        AuthUser authUser = response.getData();
        log.info("获取第三方用户信息成功：type={}, openId={}, nickname={}", 
                oauthType, authUser.getUuid(), authUser.getNickname());
        
        return authUser;
    }

    /**
     * 获取第三方平台中文名
     */
    private String getOauthTypeName(String oauthType) {
        return switch (oauthType.toLowerCase()) {
            case "wechat" -> "微信";
            case "alipay" -> "支付宝";
            case "github" -> "GitHub";
            default -> oauthType;
        };
    }

    /**
     * 构建登录响应
     */
    private LoginVo buildLoginVo(User user) {
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getPhone());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getPhone());

        LoginVo vo = new LoginVo();
        vo.setAccessToken(accessToken);
        vo.setRefreshToken(refreshToken);
        vo.setExpiresIn(jwtUtil.getAccessTokenExpiration());
        vo.setUserId(user.getId());
        vo.setPhone(user.getPhone());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());

        log.info("用户登录成功：userId={}, phone={}", user.getId(), user.getPhone());
        return vo;
    }

    private AuthRequest getAuthRequest(String oauthType) {
        return switch (oauthType.toLowerCase()) {
            case "wechat" -> {
                AuthConfig config = AuthConfig.builder()
                        .clientId(wechatAppId)
                        .clientSecret(wechatAppSecret)
                        .redirectUri(redirectUri)
                        .build();
                yield new AuthWeChatOpenRequest(config);
            }
            case "alipay" -> {
                AuthConfig config = AuthConfig.builder()
                        .clientId(alipayAppId)
//                        .clientSecret(alipayPrivateKey)
//                        .alipayPublicKey(alipayPublicKey)
                        .clientSecret(alipayPublicKey)
                        .redirectUri(redirectUri)
                        .build();
                yield new AuthAlipayRequest(config);
            }
            case "github" -> {
                AuthConfig config = AuthConfig.builder()
                        .clientId(githubClientId)
                        .clientSecret(githubClientSecret)
                        .redirectUri(redirectUri)
                        .build();
                yield new AuthGithubRequest(config);
            }
            default -> throw new RuntimeException("不支持的第三方登录方式：" + oauthType);
        };
    }
}
