package com.example.orchardcommon.config;

import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilder objectMapperBuilder() {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        SimpleModule module = new SimpleModule();
        // 注册自定义Long序列化器
        module.addSerializer(Long.class, new LongToStringSerializer());
        module.addSerializer(Long.TYPE, new LongToStringSerializer()); // 基础类型long
        builder.modules(module);
        return builder;
    }
}
