package com.example.orchardauth.config;

import com.example.orchardauth.interceptor.AuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/auth/login/**",      // 登录接口
                        "/auth/register",      // 注册接口
                        "/auth/sms/send",      // 发送验证码
                        "/auth/refresh",       // 刷新Token
                        "/auth/oauth/**",      // 第三方登录
                        "/swagger-ui.html",    // Swagger UI 入口
                        "/swagger-ui/**",      // Swagger UI 资源
                        "/v3/api-docs/**",     // OpenAPI 文档
                        "/favicon.ico",        // 图标
                        "/error"               // 错误页面
                );
    }
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // 给所有带 @RestController 的接口统一加 /admin-api 前缀
        configurer.addPathPrefix("/admin-api", c -> c.isAnnotationPresent(RestController.class));
    }
}
