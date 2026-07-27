package com.example.orchardcommon.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * 仅Long类型序列化输出字符串，解决雪花ID JS精度丢失
 * Integer/int 不受影响，正常输出数字
 */
public class LongToStringSerializer extends JsonSerializer<Long> {
    @Override
    public void serialize(Long value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }
        // Long 统一输出字符串
        gen.writeString(value.toString());
    }
}
