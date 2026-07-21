package com.example.orchardcommon.aspect;

import com.example.orchardcommon.annotation.LogOperation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * 操作日志切面
 */
@Log4j2
@Aspect
@Component
public class LogOperationAspect {

    private static final org.apache.logging.log4j.Logger OPERATION_LOGGER = 
            org.apache.logging.log4j.LogManager.getLogger("OPERATION_LOG");

    @Pointcut("@annotation(com.example.orchardcommon.annotation.LogOperation)")
    public void logPointcut() {
    }

    @Around("logPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String traceId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        
        // 获取方法信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        LogOperation logAnnotation = method.getAnnotation(LogOperation.class);
        
        // 获取请求信息
        HttpServletRequest request = getRequest();
        String requestUri = request != null ? request.getRequestURI() : "unknown";
        String httpMethod = request != null ? request.getMethod() : "unknown";
        String clientIp = request != null ? getClientIp(request) : "unknown";
        
        // 方法信息
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = method.getName();
        String args = getArgsString(joinPoint.getArgs());
        
        // 记录操作开始
        OPERATION_LOGGER.info("[{}] 开始 - {} {} | IP: {} | 模块: {} | 操作: {} | 方法: {}.{} | 参数: {}", 
                traceId, httpMethod, requestUri, clientIp,
                logAnnotation.module(), logAnnotation.description(),
                className, methodName, args);
        
        Object result = null;
        try {
            result = joinPoint.proceed();
            long costTime = System.currentTimeMillis() - startTime;
            
            // 记录操作成功
            OPERATION_LOGGER.info("[{}] 完成 - {} {} | 耗时: {}ms | 结果: {}", 
                    traceId, httpMethod, requestUri, costTime, 
                    result != null ? result.toString().substring(0, Math.min(result.toString().length(), 200)) : "void");
            
            return result;
        } catch (Throwable e) {
            long costTime = System.currentTimeMillis() - startTime;
            
            // 记录操作失败
            OPERATION_LOGGER.error("[{}] 失败 - {} {} | 耗时: {}ms | 异常: {}", 
                    traceId, httpMethod, requestUri, costTime, e.getMessage());
            throw e;
        }
    }

    private HttpServletRequest getRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理时取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    private String getArgsString(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            if (args[i] != null) {
                String argStr = args[i].toString();
                sb.append(argStr.length() > 100 ? argStr.substring(0, 100) + "..." : argStr);
            } else {
                sb.append("null");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}
