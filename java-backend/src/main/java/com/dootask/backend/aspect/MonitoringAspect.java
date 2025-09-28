package com.dootask.backend.aspect;

import com.dootask.backend.monitor.ApplicationMetrics;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * 监控切面
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class MonitoringAspect {

    private final ApplicationMetrics applicationMetrics;

    /**
     * 监控所有控制器请求
     */
    @Around("execution(* com.dootask.backend.controller..*(..))")
    public Object monitorControllerMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        // 记录请求
        applicationMetrics.recordRequest();

        // 开始计时
        Timer.Sample sample = applicationMetrics.startRequestTimer();

        try {
            Object result = joinPoint.proceed();
            log.debug("控制器方法执行成功: {}.{}", className, methodName);
            return result;
        } catch (Exception e) {
            // 记录错误
            applicationMetrics.recordError();
            log.error("控制器方法执行失败: {}.{}", className, methodName, e);
            throw e;
        } finally {
            // 停止计时
            applicationMetrics.stopRequestTimer(sample);
        }
    }

    /**
     * 监控数据库操作
     */
    @Around("execution(* com.dootask.backend.mapper..*(..))")
    public Object monitorMapperMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        // 开始数据库计时
        Timer.Sample sample = applicationMetrics.startDatabaseTimer();

        try {
            Object result = joinPoint.proceed();
            log.debug("数据库操作执行成功: {}.{}", className, methodName);
            return result;
        } catch (Exception e) {
            log.error("数据库操作执行失败: {}.{}", className, methodName, e);
            throw e;
        } finally {
            // 停止数据库计时
            applicationMetrics.stopDatabaseTimer(sample);
        }
    }

    /**
     * 监控登录操作
     */
    @Around("execution(* com.dootask.backend.service..*.login(..))")
    public Object monitorLoginMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            Object result = joinPoint.proceed();

            // 登录成功
            applicationMetrics.recordLogin();
            log.info("用户登录成功");

            return result;
        } catch (Exception e) {
            log.warn("用户登录失败: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 监控登出操作
     */
    @Around("execution(* com.dootask.backend.service..*.logout(..))")
    public Object monitorLogoutMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            Object result = joinPoint.proceed();

            // 登出成功
            applicationMetrics.recordLogout();
            log.info("用户登出成功");

            return result;
        } catch (Exception e) {
            log.warn("用户登出失败: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 监控缓存操作
     */
    @Around("@annotation(org.springframework.cache.annotation.Cacheable)")
    public Object monitorCacheableMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();

        Timer.Sample sample = applicationMetrics.startRequestTimer();

        try {
            Object result = joinPoint.proceed();
            applicationMetrics.incrementCustomCounter("cache.hit");
            return result;
        } catch (Exception e) {
            applicationMetrics.incrementCustomCounter("cache.miss");
            throw e;
        } finally {
            // 记录缓存操作时间
            applicationMetrics.stopRequestTimer(sample);
        }
    }
}