package com.example.orchardcommon.aspect;

import com.example.orchardcommon.result.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Agent 内部服务接口校验切面
 * 校验 X-Service-Key 请求头，并将 agent 携带的 userId 写入 request attribute
 */
@Slf4j
@Aspect
@Component
public class InternalApiAspect {

    @Value("${internal.service-key:orchard-agent-secret-key}")
    private String serviceKey;

    private static final String HEADER_SERVICE_KEY = "X-Service-Key";
    private static final String HEADER_USER_ID = "X-User-Id";

    @Pointcut("@annotation(com.example.orchardcommon.annotation.InternalApi) || " +
              "@within(com.example.orchardcommon.annotation.InternalApi)")
    public void internalApiPointcut() {
    }

    @Around("internalApiPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new RuntimeException("无法获取请求上下文");
        }

        HttpServletRequest request = attributes.getRequest();
        HttpServletResponse response = attributes.getResponse();

        // 校验服务密钥
        String key = request.getHeader(HEADER_SERVICE_KEY);
        if (!StringUtils.hasText(key) || !key.equals(serviceKey)) {
            log.warn("内部接口鉴权失败，无效的服务密钥，URI: {}", request.getRequestURI());
            if (response != null) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":403,\"msg\":\"无效的服务密钥\",\"success\":false}");
                return null;
            }
            throw new RuntimeException("无效的服务密钥");
        }

        // 如果 agent 携带了 userId，写入 request attribute 供后续业务使用
        String userIdStr = request.getHeader(HEADER_USER_ID);
        if (StringUtils.hasText(userIdStr)) {
            try {
                Long userId = Long.parseLong(userIdStr);
                request.setAttribute("userId", userId);
                log.debug("内部接口调用，设置 userId: {}, URI: {}", userId, request.getRequestURI());
            } catch (NumberFormatException e) {
                log.warn("内部接口调用，X-User-Id 格式错误: {}, URI: {}", userIdStr, request.getRequestURI());
            }
        }

        log.info("内部接口调用通过，URI: {}", request.getRequestURI());
        return joinPoint.proceed();
    }
}
