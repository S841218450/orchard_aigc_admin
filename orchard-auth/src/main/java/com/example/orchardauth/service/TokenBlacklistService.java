package com.example.orchardauth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Token黑名单服务（基于Redis）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final StringRedisTemplate redisTemplate;

    private static final String BLACKLIST_PREFIX = "token:blacklist:";

    /**
     * 将Token加入黑名单
     */
    public void addToBlacklist(String token, long expireSeconds) {
        try {
            redisTemplate.opsForValue().set(
                    BLACKLIST_PREFIX + token,
                    "1",
                    expireSeconds,
                    TimeUnit.SECONDS
            );
            log.debug("Token已加入黑名单: {}", token.substring(0, Math.min(20, token.length())) + "...");
        } catch (Exception e) {
            log.warn("Redis连接失败，无法将Token加入黑名单: {}", e.getMessage());
        }
    }

    /**
     * 检查Token是否在黑名单中
     */
    public boolean isBlacklisted(String token) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + token));
        } catch (Exception e) {
            log.warn("Redis连接失败，假设Token未在黑名单中: {}", e.getMessage());
            return false;
        }
    }
}
