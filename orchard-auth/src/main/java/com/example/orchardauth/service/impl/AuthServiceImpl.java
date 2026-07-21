package com.example.orchardauth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.orchardauth.dto.*;
import com.example.orchardauth.service.AuthService;
import com.example.orchardauth.service.SmsCodeService;
import com.example.orchardauth.service.TokenBlacklistService;
import com.example.orchardauth.util.JwtUtil;
import com.example.orchardauth.vo.LoginVo;
import com.example.orchardauth.vo.UserInfoVo;
import com.example.orchardcommon.business.SnowflakeId.BizCodeEnum;
import com.example.orchardcommon.business.SnowflakeId.SnowflakeUtils;
import com.example.orchardusermanagement.entity.User;
import com.example.orchardusermanagement.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final SmsCodeService smsCodeService;
    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public LoginVo loginByPassword(LoginByPasswordDto dto) {
        // 查找用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, dto.getPhone());
        User user = userMapper.selectOne(wrapper);
        
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 验证密码
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误");
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
    public LoginVo loginBySms(LoginBySmsDto dto) {
        // 验证验证码
        smsCodeService.verifyCode(dto.getPhone(), dto.getCode());

        // 查找用户，不存在则自动注册
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, dto.getPhone());
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            // 自动注册
            user = new User();
            user.setId(SnowflakeUtils.nextId(BizCodeEnum.USER));
            user.setPhone(dto.getPhone());
            user.setNickname("用户" + dto.getPhone().substring(7));
            user.setStatus(1);
            user.setCreateTime(LocalDateTime.now());
            userMapper.insert(user);
            log.info("验证码登录自动注册：phone={}", dto.getPhone());
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
    public void register(RegisterDto dto) {
        // 验证验证码
        smsCodeService.verifyCode(dto.getPhone(), dto.getCode());

        // 检查手机号是否已注册
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, dto.getPhone());
        if (userMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("该手机号已注册");
        }

        // 创建用户
        User user = new User();
        user.setId(SnowflakeUtils.nextId(BizCodeEnum.USER));
        user.setPhone(dto.getPhone());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname() != null ? dto.getNickname() : "用户" + dto.getPhone().substring(7));
        user.setAvatar(dto.getAvatar());
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());

        userMapper.insert(user);
        log.info("用户注册成功：phone={}", dto.getPhone());
    }

    @Override
    public LoginVo refreshToken(RefreshTokenDto dto) {
        String refreshToken = dto.getRefreshToken();

        // 验证 Refresh Token
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new RuntimeException("刷新Token无效或已过期");
        }

        // 检查是否为 Refresh Token
        String tokenType = jwtUtil.getTokenType(refreshToken);
        if (!"refresh".equals(tokenType)) {
            throw new RuntimeException("无效的刷新Token");
        }

        // 获取用户信息
        Long userId = jwtUtil.getUserId(refreshToken);
        String phone = jwtUtil.getPhone(refreshToken);

        // 查找用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 检查状态
        if (user.getStatus() != 1) {
            throw new RuntimeException("账号已被禁用");
        }

        // 将旧的 Refresh Token 加入黑名单
        tokenBlacklistService.addToBlacklist(refreshToken, 7 * 24 * 3600);

        return buildLoginVo(user);
    }

    @Override
    public void logout(String accessToken) {
        if (accessToken != null && accessToken.startsWith("Bearer ")) {
            accessToken = accessToken.substring(7);
        }
        
        // 将 Access Token 加入黑名单
        if (accessToken != null && jwtUtil.validateToken(accessToken)) {
            long expiration = jwtUtil.getAccessTokenExpiration();
            tokenBlacklistService.addToBlacklist(accessToken, expiration);
        }

        log.info("用户退出登录");
    }

    @Override
    public UserInfoVo getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        UserInfoVo vo = new UserInfoVo();
        vo.setUserId(user.getId());
        vo.setPhone(user.getPhone());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setStatus(user.getStatus());

        return vo;
    }

    @Override
    public void bindPhone(Long userId, BindPhoneDto dto) {
        // 验证验证码
        smsCodeService.verifyCode(dto.getPhone(), dto.getCode());

        // 检查手机号是否已被其他用户绑定
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, dto.getPhone());
        User existUser = userMapper.selectOne(wrapper);
        if (existUser != null && !existUser.getId().equals(userId)) {
            throw new RuntimeException("该手机号已被其他账号绑定");
        }

        // 绑定手机号
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setPhone(dto.getPhone());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        log.info("绑定手机号成功：userId={}, phone={}", userId, dto.getPhone());
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
}
