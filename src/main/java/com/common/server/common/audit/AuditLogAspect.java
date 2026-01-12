package com.common.server.common.audit;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * 감사 로그 AOP Aspect
 *
 * @AuditLog 어노테이션이 붙은 메서드의 호출을 감사 로그로 기록합니다.
 *
 * @author Common Server Framework
 * @since 2025-01-13
 */
@Aspect
@Component
@Slf4j
public class AuditLogAspect {

    @Around("@annotation(auditLog)")
    public Object logAudit(ProceedingJoinPoint joinPoint, AuditLog auditLog) throws Throwable {
        LocalDateTime startTime = LocalDateTime.now();
        String userId = getCurrentUserId();
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        Object[] args = joinPoint.getArgs();

        log.info("[AUDIT] START | User: {} | Action: {} | Resource: {} | Method: {}.{} | Args: {}",
                userId,
                auditLog.action(),
                auditLog.resource(),
                className,
                methodName,
                sanitizeArgs(args));

        try {
            Object result = joinPoint.proceed();

            log.info("[AUDIT] SUCCESS | User: {} | Action: {} | Resource: {} | Method: {}.{} | Duration: {}ms",
                    userId,
                    auditLog.action(),
                    auditLog.resource(),
                    className,
                    methodName,
                    java.time.Duration.between(startTime, LocalDateTime.now()).toMillis());

            return result;

        } catch (Exception e) {
            log.error("[AUDIT] FAILED | User: {} | Action: {} | Resource: {} | Method: {}.{} | Error: {} | Duration: {}ms",
                    userId,
                    auditLog.action(),
                    auditLog.resource(),
                    className,
                    methodName,
                    e.getMessage(),
                    java.time.Duration.between(startTime, LocalDateTime.now()).toMillis());
            throw e;
        }
    }

    private String getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                return authentication.getName();
            }
        } catch (Exception e) {
            log.debug("Failed to get current user ID", e);
        }
        return "anonymous";
    }

    private String sanitizeArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }

        return Arrays.stream(args)
                .map(arg -> {
                    if (arg == null) {
                        return "null";
                    }
                    String argStr = arg.toString();
                    // 민감 정보 마스킹 (비밀번호 등)
                    if (argStr.toLowerCase().contains("password") ||
                            argStr.toLowerCase().contains("secret") ||
                            argStr.toLowerCase().contains("token")) {
                        return "[MASKED]";
                    }
                    // 너무 긴 값은 자르기
                    if (argStr.length() > 100) {
                        return argStr.substring(0, 100) + "...";
                    }
                    return argStr;
                })
                .toList()
                .toString();
    }
}
