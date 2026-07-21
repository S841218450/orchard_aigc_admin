package com.example.orchardcommon.annotation;

import java.lang.annotation.*;

/**
 * 内部服务接口注解
 * 标记此注解的接口为内部服务调用，跳过用户鉴权
 * Agent 端通过 X-Service-Key 请求头进行服务密钥校验
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface InternalApi {
}
