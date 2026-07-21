package com.example.orchardauth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 短信验证码服务（基于Redis）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmsCodeService {

    private final StringRedisTemplate redisTemplate;

    private static final String SMS_CODE_PREFIX = "sms:code:";
    private static final String SMS_LIMIT_PREFIX = "sms:limit:";
    private static final long CODE_EXPIRE_MINUTES = 5;
    private static final int DAILY_LIMIT = 10; // 每天最多10次

    /**
     * 发送验证码
     */
    public String sendCode(String phone) {
        // 检查每日发送次数限制
        String limitKey = SMS_LIMIT_PREFIX + phone;
        String limitCount = redisTemplate.opsForValue().get(limitKey);
        if (limitCount != null && Integer.parseInt(limitCount) >= DAILY_LIMIT) {
            throw new RuntimeException("今日验证码发送次数已达上限");
        }

        // 生成6位验证码
        String code = String.format("%06d", new Random().nextInt(1000000));

        // 存入Redis，5分钟过期
        String codeKey = SMS_CODE_PREFIX + phone;
        redisTemplate.opsForValue().set(codeKey, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);

        // 增加每日发送次数
        if (limitCount == null) {
            redisTemplate.opsForValue().set(limitKey, "1", 24, TimeUnit.HOURS);
        } else {
            redisTemplate.opsForValue().increment(limitKey);
        }

        // TODO: 接入真实短信服务（阿里云/腾讯云）
        log.info("发送验证码：phone={}, code={}", phone, code);

        return code;
    }

    /**
     * 验证验证码
     */
    public boolean verifyCode(String phone, String code) {
        String key = SMS_CODE_PREFIX + phone;
        String storedCode = redisTemplate.opsForValue().get(key);
        if (storedCode == null) {
            throw new RuntimeException("验证码已过期，请重新获取");
        }
        if (!storedCode.equals(code)) {
            throw new RuntimeException("验证码错误");
        }
        // 验证成功后删除
        redisTemplate.delete(key);
        return true;
    }
}
