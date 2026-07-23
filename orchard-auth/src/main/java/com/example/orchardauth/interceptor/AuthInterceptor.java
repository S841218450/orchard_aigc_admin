package com.example.orchardauth.interceptor;

import com.example.orchardauth.util.JwtUtil;
import com.example.orchardcommon.annotation.InternalApi;
import com.example.orchardcommon.annotation.PublicApi;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.debug("AuthInterceptor - 请求路径: {}, handler类型: {}", request.getRequestURI(), handler.getClass().getName());
        
        if (handler instanceof HandlerMethod handlerMethod) {
            log.debug("AuthInterceptor - 方法: {}, 类: {}", handlerMethod.getMethod().getName(), handlerMethod.getBeanType().getName());
            log.debug("AuthInterceptor - hasPublicApi注解: {}, hasInternalApi注解: {}", 
                    handlerMethod.hasMethodAnnotation(PublicApi.class),
                    handlerMethod.hasMethodAnnotation(InternalApi.class));
            
            // 检查是否为 @PublicApi 注解的接口（完全跳过鉴权）
            if (handlerMethod.hasMethodAnnotation(PublicApi.class)
                    || handlerMethod.getBeanType().isAnnotationPresent(PublicApi.class)) {
                log.info("AuthInterceptor - 跳过鉴权（@PublicApi）: {}", request.getRequestURI());
                return true;
            }
            // 检查是否为 @InternalApi 注解的接口（跳过用户鉴权，由切面校验服务密钥）
            if (handlerMethod.hasMethodAnnotation(InternalApi.class)
                    || handlerMethod.getBeanType().isAnnotationPresent(InternalApi.class)) {
                log.info("AuthInterceptor - 跳过用户鉴权（@InternalApi）: {}", request.getRequestURI());
                return true;
            }
        } else {
            log.warn("AuthInterceptor - handler不是HandlerMethod类型: {}", handler.getClass().getName());
        }

        // 获取 token
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"未登录\",\"success\":false}");
            return false;
        }

        token = token.substring(7);

        // 验证 token
        if (!jwtUtil.validateToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"登录已过期\",\"success\":false}");
            return false;
        }

        // 检查是否为 Access Token
        String tokenType = jwtUtil.getTokenType(token);
        if (!"access".equals(tokenType)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"无效的访问令牌\",\"success\":false}");
            return false;
        }

        // 解析 token
        Long userId = jwtUtil.getUserId(token);
        String phone = jwtUtil.getPhone(token);

        // 将用户信息存入 request
        request.setAttribute("userId", userId);
        request.setAttribute("phone", phone);

        return true;
    }
}
