package com.example.orchardcommon.dateBase.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Redis配置类
 * 自动从 application.yml 读取 spring.data.redis 配置
 */
@Configuration
public class RedisConfig {

    /**
     * 配置 StringRedisTemplate
     * Spring Boot 会自动根据 application.yml 配置创建 RedisConnectionFactory
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(connectionFactory);
        return template;
    }
}
