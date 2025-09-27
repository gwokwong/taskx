package com.dootask.backend.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * 安全审计切面
 */
@Slf4j
@Aspect
@Component
public class SecurityAuditAspect {

    @Around("@annotation(com.dootask.backend.annotation.SecurityAudit)")
    public Object auditSecurity(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        Object[] args = joinPoint.getArgs();

        // 记录操作开始
        log.info("安全审计 - 开始执行: {}.{}", className, methodName);

        long startTime = System.currentTimeMillis();
        Object result = null;
        Exception exception = null;

        try {
            result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;

            // 记录操作结果
            if (exception != null) {
                log.warn("安全审计 - 操作失败: {}.{}, 执行时间: {}ms, 错误: {}",
                    className, methodName, executionTime, exception.getMessage());
            } else {
                log.info("安全审计 - 操作成功: {}.{}, 执行时间: {}ms",
                    className, methodName, executionTime);
            }

            // 检查敏感操作
            if (isSensitiveOperation(methodName)) {
                log.warn("安全审计 - 敏感操作: {}.{}, 参数: {}, 结果: {}",
                    className, methodName, maskSensitiveData(args), maskSensitiveResult(result));
            }
        }
    }

    /**
     * 判断是否为敏感操作
     */
    private boolean isSensitiveOperation(String methodName) {
        return methodName.contains("delete") ||
               methodName.contains("remove") ||
               methodName.contains("update") ||
               methodName.contains("create") ||
               methodName.contains("login") ||
               methodName.contains("password") ||
               methodName.contains("export") ||
               methodName.contains("import");
    }

    /**
     * 脱敏敏感数据
     */
    private Object[] maskSensitiveData(Object[] args) {
        if (args == null) return null;

        Object[] maskedArgs = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof String) {
                String str = (String) args[i];
                if (str.contains("password") || str.contains("token") || str.contains("secret")) {
                    maskedArgs[i] = "***";
                } else {
                    maskedArgs[i] = str;
                }
            } else {
                maskedArgs[i] = args[i];
            }
        }
        return maskedArgs;
    }

    /**
     * 脱敏敏感结果
     */
    private Object maskSensitiveResult(Object result) {
        if (result == null) return null;

        // 简单脱敏，实际项目中可以更复杂
        String resultStr = result.toString();
        if (resultStr.contains("password") || resultStr.contains("token") || resultStr.contains("secret")) {
            return "***";
        }
        return result;
    }
}