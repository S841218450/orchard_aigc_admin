package com.example.orchardcommon.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Configuration
public class JacksonConfig {

    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Bean
    public Jackson2ObjectMapperBuilder objectMapperBuilder() {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();

        // 1. Java 8 时间模块 — 先注册基础类型处理能力
        JavaTimeModule javaTimeModule = new JavaTimeModule();

        // 2. LocalDateTime 全局序列化为 毫秒时间戳（项目规范：VO返回时间戳Long）
        javaTimeModule.addSerializer(LocalDateTime.class, new JsonSerializer<>() {
            @Override
            public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                if (value == null) {
                    gen.writeNull();
                    return;
                }
                long ts = value.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                // Long 时间戳也会被后面的 LongToStringSerializer 转成字符串，符合前后端契约
                gen.writeNumber(ts);
            }
        });
        javaTimeModule.addDeserializer(LocalDateTime.class, new JsonDeserializer<>() {
            @Override
            public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                String text = p.getText();
                if (text == null || text.isEmpty()) {
                    return null;
                }
                if (text.chars().allMatch(Character::isDigit)) {
                    long ts = Long.parseLong(text);
                    return LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(ts), ZoneId.systemDefault());
                }
                return LocalDateTime.parse(text, DATETIME_FORMAT);
            }
        });

        // 3. LocalDate 全局序列化为 当天零点毫秒时间戳
        javaTimeModule.addSerializer(LocalDate.class, new JsonSerializer<>() {
            @Override
            public void serialize(LocalDate value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                if (value == null) {
                    gen.writeNull();
                    return;
                }
                long ts = value.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
                gen.writeNumber(ts);
            }
        });
        javaTimeModule.addDeserializer(LocalDate.class, new JsonDeserializer<>() {
            @Override
            public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                String text = p.getText();
                if (text == null || text.isEmpty()) {
                    return null;
                }
                if (text.chars().allMatch(Character::isDigit)) {
                    long ts = Long.parseLong(text);
                    return LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(ts), ZoneId.systemDefault()).toLocalDate();
                }
                return LocalDate.parse(text, DATE_FORMAT);
            }
        });

        // 4. LocalTime 全局序列化为 当天从零点开始的毫秒数
        javaTimeModule.addSerializer(LocalTime.class, new JsonSerializer<>() {
            @Override
            public void serialize(LocalTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                if (value == null) {
                    gen.writeNull();
                    return;
                }
                gen.writeNumber(value.toNanoOfDay() / 1_000_000L);
            }
        });
        javaTimeModule.addDeserializer(LocalTime.class, new JsonDeserializer<>() {
            @Override
            public LocalTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                String text = p.getText();
                if (text == null || text.isEmpty()) {
                    return null;
                }
                if (text.chars().allMatch(Character::isDigit)) {
                    long ms = Long.parseLong(text);
                    return LocalTime.ofNanoOfDay(ms * 1_000_000L);
                }
                return LocalTime.parse(text, TIME_FORMAT);
            }
        });

        // 5. 自定义 Long/long → String 序列化器（解决雪花ID JS精度丢失）
        SimpleModule longModule = new SimpleModule();
        LongToStringSerializer longSerializer = new LongToStringSerializer();
        longModule.addSerializer(Long.class, longSerializer);
        longModule.addSerializer(Long.TYPE, longSerializer);

        // 6. 关键：使用 modulesToInstall 追加注册，避免覆盖 SpringBoot 默认注册的模块
        builder.modulesToInstall(javaTimeModule, longModule);

        // 7. 禁用 WRITE_DATES_AS_TIMESTAMPS，防止默认写日期为数组[2026,7,31]
        builder.featuresToDisable(
                SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,
                SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS,
                SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS
        );

        return builder;
    }
}
