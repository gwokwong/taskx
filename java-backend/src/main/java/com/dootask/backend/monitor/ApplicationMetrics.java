package com.dootask.backend.monitor;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 应用监控指标
 */
@Slf4j
@Component
public class ApplicationMetrics {

    private final MeterRegistry meterRegistry;

    // 计数器
    private final Counter requestCounter;
    private final Counter errorCounter;
    private final Counter loginCounter;
    private final Counter logoutCounter;

    // 计时器
    private final Timer requestTimer;
    private final Timer databaseQueryTimer;

    // 原子计数器
    private final AtomicInteger activeUsers = new AtomicInteger(0);
    private final AtomicInteger activeSessions = new AtomicInteger(0);
    private final AtomicInteger queueSize = new AtomicInteger(0);

    public ApplicationMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // 初始化计数器
        this.requestCounter = Counter.builder("dootask.requests.total")
                .description("Total number of requests")
                .register(meterRegistry);

        this.errorCounter = Counter.builder("dootask.errors.total")
                .description("Total number of errors")
                .register(meterRegistry);

        this.loginCounter = Counter.builder("dootask.logins.total")
                .description("Total number of logins")
                .register(meterRegistry);

        this.logoutCounter = Counter.builder("dootask.logouts.total")
                .description("Total number of logouts")
                .register(meterRegistry);

        // 初始化计时器
        this.requestTimer = Timer.builder("dootask.request.duration")
                .description("Request processing time")
                .register(meterRegistry);

        this.databaseQueryTimer = Timer.builder("dootask.database.query.duration")
                .description("Database query execution time")
                .register(meterRegistry);

        // 注册仪表盘
        Gauge.builder("dootask.users.active", activeUsers, AtomicInteger::get)
                .description("Number of active users")
                .register(meterRegistry);

        Gauge.builder("dootask.sessions.active", activeSessions, AtomicInteger::get)
                .description("Number of active sessions")
                .register(meterRegistry);

        Gauge.builder("dootask.queue.size", queueSize, AtomicInteger::get)
                .description("Size of processing queue")
                .register(meterRegistry);

        // JVM 指标
        Gauge.builder("dootask.jvm.memory.used", Runtime.getRuntime(),
                runtime -> (double) (runtime.totalMemory() - runtime.freeMemory()))
                .description("JVM memory used")
                .register(meterRegistry);

        Gauge.builder("dootask.jvm.memory.free", Runtime.getRuntime(),
                runtime -> (double) runtime.freeMemory())
                .description("JVM memory free")
                .register(meterRegistry);

        Gauge.builder("dootask.jvm.memory.max", Runtime.getRuntime(),
                runtime -> (double) runtime.maxMemory())
                .description("JVM memory max")
                .register(meterRegistry);

        log.info("应用监控指标初始化完成");
    }

    /**
     * 记录请求
     */
    public void recordRequest() {
        requestCounter.increment();
    }

    /**
     * 记录请求处理时间
     */
    public Timer.Sample startRequestTimer() {
        return Timer.start(meterRegistry);
    }

    /**
     * 停止请求计时
     */
    public void stopRequestTimer(Timer.Sample sample) {
        sample.stop(requestTimer);
    }

    /**
     * 记录错误
     */
    public void recordError() {
        errorCounter.increment();
    }

    /**
     * 记录登录
     */
    public void recordLogin() {
        loginCounter.increment();
        activeUsers.incrementAndGet();
        activeSessions.incrementAndGet();
    }

    /**
     * 记录登出
     */
    public void recordLogout() {
        logoutCounter.increment();
        activeUsers.decrementAndGet();
        activeSessions.decrementAndGet();
    }

    /**
     * 记录数据库查询时间
     */
    public Timer.Sample startDatabaseTimer() {
        return Timer.start(meterRegistry);
    }

    /**
     * 停止数据库计时
     */
    public void stopDatabaseTimer(Timer.Sample sample) {
        sample.stop(databaseQueryTimer);
    }

    /**
     * 设置队列大小
     */
    public void setQueueSize(int size) {
        queueSize.set(size);
    }

    /**
     * 增加队列项目
     */
    public void incrementQueue() {
        queueSize.incrementAndGet();
    }

    /**
     * 减少队列项目
     */
    public void decrementQueue() {
        queueSize.decrementAndGet();
    }

    /**
     * 获取当前活跃用户数
     */
    public int getActiveUsers() {
        return activeUsers.get();
    }

    /**
     * 获取当前活跃会话数
     */
    public int getActiveSessions() {
        return activeSessions.get();
    }

    /**
     * 手动设置活跃用户数
     */
    public void setActiveUsers(int count) {
        activeUsers.set(count);
    }

    /**
     * 手动设置活跃会话数
     */
    public void setActiveSessions(int count) {
        activeSessions.set(count);
    }

    /**
     * 记录自定义指标
     */
    public void recordCustomMetric(String name, double value) {
        Gauge.builder("dootask.custom." + name, () -> value)
                .description("Custom metric: " + name)
                .register(meterRegistry);
    }

    /**
     * 增加自定义计数器
     */
    public void incrementCustomCounter(String name) {
        Counter.builder("dootask.custom.counter." + name)
                .description("Custom counter: " + name)
                .register(meterRegistry)
                .increment();
    }
}