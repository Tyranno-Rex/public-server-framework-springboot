package com.common.server.common.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 분산 락 AOP Aspect
 *
 * @DistributedLock 어노테이션이 붙은 메서드에 분산 락을 적용합니다.
 *
 * @author Common Server Framework
 * @since 2025-01-13
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class DistributedLockAspect {

    private final DistributedLockService lockService;
    private final ExpressionParser parser = new SpelExpressionParser();
    private final DefaultParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();

    @Around("@annotation(distributedLock)")
    public Object around(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // SpEL 표현식으로 락 키 생성
        String lockKey = generateLockKey(distributedLock.key(), method, joinPoint.getArgs());

        log.debug("Attempting to acquire lock: {}", lockKey);

        try {
            return lockService.executeWithLock(
                    lockKey,
                    distributedLock.waitTime(),
                    distributedLock.leaseTime(),
                    distributedLock.timeUnit(),
                    () -> {
                        try {
                            return joinPoint.proceed();
                        } catch (Throwable e) {
                            throw new RuntimeException(e);
                        }
                    }
            );
        } catch (LockAcquisitionException e) {
            if (distributedLock.throwOnFailure()) {
                throw e;
            }
            log.warn("Lock acquisition failed for key: {}, returning null", lockKey);
            return null;
        }
    }

    private String generateLockKey(String keyExpression, Method method, Object[] args) {
        // 단순 문자열인 경우 그대로 반환
        if (!keyExpression.contains("#")) {
            return keyExpression.replace("'", "");
        }

        // SpEL 표현식 파싱
        EvaluationContext context = new StandardEvaluationContext();
        String[] paramNames = nameDiscoverer.getParameterNames(method);

        if (paramNames != null) {
            for (int i = 0; i < paramNames.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
        }

        try {
            Object result = parser.parseExpression(keyExpression).getValue(context);
            return result != null ? result.toString() : keyExpression;
        } catch (Exception e) {
            log.warn("Failed to parse lock key expression: {}, using raw expression", keyExpression);
            return keyExpression;
        }
    }
}
