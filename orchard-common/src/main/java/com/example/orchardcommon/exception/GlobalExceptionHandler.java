package com.example.orchardcommon.exception;

import com.example.orchardcommon.result.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Log4j2
@RestControllerAdvice(basePackages = "com.example")
public class GlobalExceptionHandler {

    @ExceptionHandler(NoResourceFoundException.class)
    public Result<?> handleNoResourceFound(NoResourceFoundException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.warn("[404] 资源不存在 - {} {}", request.getMethod(), requestURI);
        return Result.error(404, "资源不存在");
    }

    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e, HttpServletRequest request) {
        // 排除 springdoc 的路径
        String requestURI = request.getRequestURI();
        if (requestURI.contains("/v3/api-docs") || requestURI.contains("/swagger-ui")) {
            log.error("[Swagger] 异常：{}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
        String clientIp = getClientIp(request);
        log.error("[异常] {} {} | IP: {} | 异常: {}", request.getMethod(), requestURI, clientIp, e.getMessage(), e);
        return Result.error("系统繁忙，请稍后重试");
    }

    @ExceptionHandler(RuntimeException.class)
    public Result<?> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        if (requestURI.contains("/v3/api-docs") || requestURI.contains("/swagger-ui")) {
            log.error("[Swagger] 运行时异常：{}", e.getMessage(), e);
            throw e;
        }
        String clientIp = getClientIp(request);
        log.error("[运行时异常] {} {} | IP: {} | 异常: {}", request.getMethod(), requestURI, clientIp, e.getMessage(), e);
        return Result.error("服务器异常，请联系管理员");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        String message = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        String requestURI = request.getRequestURI();
        log.warn("[参数校验] {} {} | 失败: {}", request.getMethod(), requestURI, message);
        return Result.error("缺少必要的参数，参数校验不通过");
    }

    @ExceptionHandler(BindException.class)
    public Result<?> handleBindException(BindException e, HttpServletRequest request) {
        String message = e.getAllErrors().get(0).getDefaultMessage();
        String requestURI = request.getRequestURI();
        log.warn("[参数绑定] {} {} | 失败: {}", request.getMethod(), requestURI, message);
        return Result.error("参数绑定失败");
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
