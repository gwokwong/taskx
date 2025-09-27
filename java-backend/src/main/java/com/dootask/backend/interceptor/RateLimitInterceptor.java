package com.dootask.backend.interceptor;

import com.github.benmanes.caffeine.cache.Cache;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;

/**
 * 限流拦截器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final Cache<String, Object> rateLimitCache;

    // 默认限制：每分钟100次请求
    private static final int DEFAULT_LIMIT = 100;
    private static final int TIME_WINDOW = 60; // 秒

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String clientId = getClientId(request);
        String key = "rate_limit:" + clientId;

        // 获取当前计数
        RateLimitInfo rateLimitInfo = (RateLimitInfo) rateLimitCache.getIfPresent(key);

        if (rateLimitInfo == null) {
            // 首次请求
            rateLimitInfo = new RateLimitInfo(1, System.currentTimeMillis());
            rateLimitCache.put(key, rateLimitInfo);
            return true;
        }

        long currentTime = System.currentTimeMillis();
        long timeDiff = (currentTime - rateLimitInfo.getStartTime()) / 1000;

        if (timeDiff >= TIME_WINDOW) {
            // 时间窗口重置
            rateLimitInfo = new RateLimitInfo(1, currentTime);
            rateLimitCache.put(key, rateLimitInfo);
            return true;
        }

        if (rateLimitInfo.getCount() >= DEFAULT_LIMIT) {
            // 超出限制
            log.warn("客户端 {} 触发限流，当前请求数: {}", clientId, rateLimitInfo.getCount());

            response.setStatus(429);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\":\"请求过于频繁，请稍后再试\",\"code\":429}");
            return false;
        }

        // 增加计数
        rateLimitInfo.incrementCount();
        rateLimitCache.put(key, rateLimitInfo);

        // 添加响应头
        response.setHeader("X-RateLimit-Limit", String.valueOf(DEFAULT_LIMIT));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(DEFAULT_LIMIT - rateLimitInfo.getCount()));
        response.setHeader("X-RateLimit-Reset", String.valueOf(rateLimitInfo.getStartTime() + TIME_WINDOW * 1000));

        return true;
    }

    /**
     * 获取客户端标识
     */
    private String getClientId(HttpServletRequest request) {
        // 优先使用用户ID
        String userId = request.getHeader("X-User-ID");
        if (userId != null && !userId.isEmpty()) {
            return "user:" + userId;
        }

        // 使用IP地址
        String ip = getClientIpAddress(request);
        return "ip:" + ip;
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty() && !"unknown".equalsIgnoreCase(xRealIP)) {
            return xRealIP;
        }

        return request.getRemoteAddr();
    }

    /**
     * 限流信息类
     */
    private static class RateLimitInfo {
        private int count;
        private final long startTime;

        public RateLimitInfo(int count, long startTime) {
            this.count = count;
            this.startTime = startTime;
        }

        public int getCount() {
            return count;
        }

        public long getStartTime() {
            return startTime;
        }

        public void incrementCount() {
            this.count++;
        }
    }
}