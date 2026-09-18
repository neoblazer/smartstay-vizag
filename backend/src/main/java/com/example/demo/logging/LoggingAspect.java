package com.example.demo.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger =
            LoggerFactory.getLogger(LoggingAspect.class);

    @Pointcut("execution(* com.example.demo.service..*(..)) || execution(* com.example.demo.controller..*(..))")
    public void applicationLayer() {}

    @Around("applicationLayer()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {

        String methodName = joinPoint.getSignature().toShortString();
        Object[] args = maskSensitiveArgs(joinPoint.getArgs());

        logger.info("➡️ Entering: {} | Args: {}", methodName, Arrays.toString(args));

        long start = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();

            long timeTaken = System.currentTimeMillis() - start;

            String resultString = safeDescribe(result);

            logger.info("✅ Exiting: {} | Result: {} | ⏱️ {} ms",
                    methodName, resultString, timeTaken);

            return result;

        } catch (Exception ex) {
            long timeTaken = System.currentTimeMillis() - start;

            logger.error("❌ Exception in: {} | ⏱️ {} ms | Message: {}",
                    methodName, timeTaken, ex.getMessage(), ex);

            throw ex;
        }
    }

    private Object[] maskSensitiveArgs(Object[] args) {
        return Arrays.stream(args)
                .map(this::safeDescribeArg)
                .toArray();
    }

    private Object safeDescribeArg(Object arg) {
        if (arg == null) return null;

        String className = arg.getClass().getSimpleName();

        if (arg instanceof String s) {
            String lower = s.toLowerCase();
            if (lower.contains("password") || lower.contains("token")) {
                return "***masked***";
            }
            return s.length() > 150 ? s.substring(0, 150) + "...(truncated)" : s;
        }

        if (arg instanceof Number || arg instanceof Boolean) {
            return arg;
        }

        if (arg instanceof Collection<?> c) {
            return className + "(size=" + c.size() + ")";
        }

        if (arg instanceof Map<?, ?> m) {
            return className + "(size=" + m.size() + ")";
        }

        return className;
    }

    private String safeDescribe(Object result) {
        if (result == null) return "null";

        if (result instanceof ResponseEntity<?> response) {
            Object body = response.getBody();
            return "ResponseEntity(status=" + response.getStatusCode() +
                    ", body=" + safeDescribe(body) + ")";
        }

        if (result instanceof String s) {
            String lower = s.toLowerCase();
            if (lower.contains("password") || lower.contains("token")) {
                return "***masked***";
            }
            return s.length() > 200 ? s.substring(0, 200) + "...(truncated)" : s;
        }

        if (result instanceof Number || result instanceof Boolean) {
            return String.valueOf(result);
        }

        if (result instanceof Collection<?> c) {
            return result.getClass().getSimpleName() + "(size=" + c.size() + ")";
        }

        if (result instanceof Map<?, ?> m) {
            return result.getClass().getSimpleName() + "(size=" + m.size() + ")";
        }

        return result.getClass().getSimpleName();
    }
}