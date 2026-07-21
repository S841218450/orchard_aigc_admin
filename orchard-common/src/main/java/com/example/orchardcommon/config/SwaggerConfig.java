package com.example.orchardcommon.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Orchard 2026 API")
                        .version("1.0.0")
                        .description("果园管理系统接口文档")
                        .contact(new Contact().name("西米")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Auth"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Auth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }

    /**
     * 认证模块 API
     */
    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("1-认证管理")
                .pathsToMatch("/auth/**")
                .build();
    }

    /**
     * 用户管理模块 API
     */
    @Bean
    public GroupedOpenApi userManagementApi() {
        return GroupedOpenApi.builder()
                .group("2-用户管理")
                .pathsToMatch("/company/**", "/department/**", "/user/**", "/plan/**", "/subscription/**")
                .build();
    }

    /**
     * AI 模块 API
     */
    @Bean
    public GroupedOpenApi aiApi() {
        return GroupedOpenApi.builder()
                .group("3-AI对话")
                .pathsToMatch("/api/ai/**")
                .build();
    }

    /**
     * 文件管理模块 API
     */
    @Bean
    public GroupedOpenApi fileApi() {
        return GroupedOpenApi.builder()
                .group("4-文件管理")
                .pathsToMatch("/file/**")
                .build();
    }
}
