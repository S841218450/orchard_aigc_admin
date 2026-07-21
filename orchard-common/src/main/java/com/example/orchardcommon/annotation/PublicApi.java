package com.example.orchardcommon.annotation;

import java.lang.annotation.*;

/**
 * 公开接口注解
 * 标记此注解的接口不需要登录验证
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PublicApi {
}
