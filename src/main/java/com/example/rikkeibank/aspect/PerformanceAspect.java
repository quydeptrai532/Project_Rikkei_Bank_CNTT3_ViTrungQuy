package com.example.rikkeibank.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Aspect
@Component
@Slf4j
public class PerformanceAspect {

    @Around("execution(* com.example.rikkeibank.controller..*(..)) || execution(* com.example.rikkeibank.service..*(..))")
    public Object profileMethodExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Object result = joinPoint.proceed();

        stopWatch.stop();
        long executionTime = stopWatch.getTotalTimeMillis();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        if (executionTime > 2000) {
            log.warn("[PERFORMANCE WARNING] {}.{} mất {} ms để thực thi (> 2s)", className, methodName, executionTime);
        } else {
            log.debug("[PERFORMANCE] {}.{} thực thi trong {} ms", className, methodName, executionTime);
        }

        return result;
    }
}