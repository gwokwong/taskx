package com.dootask.backend.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * 缓存配置
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Caffeine 缓存管理器
     */
    @Bean
    public CaffeineCacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(caffeineCacheBuilder());

        // 预定义缓存
        cacheManager.setCacheNames(Arrays.asList(
            "users",           // 用户缓存
            "projects",        // 项目缓存
            "tasks",           // 任务缓存
            "departments",     // 部门缓存
            "permissions",     // 权限缓存
            "settings",        // 系统设置缓存
            "search",          // 搜索结果缓存
            "statistics"       // 统计数据缓存
        ));

        return cacheManager;
    }

    /**
     * Caffeine 缓存构建器
     */
    private Caffeine<Object, Object> caffeineCacheBuilder() {
        return Caffeine.newBuilder()
            .initialCapacity(50)
            .maximumSize(500)
            .expireAfterWrite(Duration.ofMinutes(10))
            .expireAfterAccess(Duration.ofMinutes(5))
            .recordStats();
    }

    /**
     * 用户权限缓存 - 长期缓存
     */
    @Bean("permissionCache")
    public Cache<String, Object> permissionCache() {
        return Caffeine.newBuilder()
            .initialCapacity(100)
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofHours(1))
            .expireAfterAccess(Duration.ofMinutes(30))
            .recordStats()
            .build();
    }

    /**
     * 会话缓存 - 短期缓存
     */
    @Bean("sessionCache")
    public Cache<String, Object> sessionCache() {
        return Caffeine.newBuilder()
            .initialCapacity(200)
            .maximumSize(2000)
            .expireAfterWrite(Duration.ofMinutes(30))
            .expireAfterAccess(Duration.ofMinutes(15))
            .recordStats()
            .build();
    }

    /**
     * 验证码缓存 - 极短期缓存
     */
    @Bean("verificationCache")
    public Cache<String, Object> verificationCache() {
        return Caffeine.newBuilder()
            .initialCapacity(50)
            .maximumSize(500)
            .expireAfterWrite(Duration.ofMinutes(5))
            .recordStats()
            .build();
    }

    /**
     * 搜索结果缓存 - 中期缓存
     */
    @Bean("searchCache")
    public Cache<String, Object> searchCache() {
        return Caffeine.newBuilder()
            .initialCapacity(100)
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(15))
            .expireAfterAccess(Duration.ofMinutes(5))
            .recordStats()
            .build();
    }

    /**
     * 文件元数据缓存
     */
    @Bean("fileMetadataCache")
    public Cache<String, Object> fileMetadataCache() {
        return Caffeine.newBuilder()
            .initialCapacity(200)
            .maximumSize(2000)
            .expireAfterWrite(Duration.ofHours(2))
            .expireAfterAccess(Duration.ofMinutes(30))
            .recordStats()
            .build();
    }

    /**
     * 系统配置缓存 - 长期缓存
     */
    @Bean("configCache")
    public Cache<String, Object> configCache() {
        return Caffeine.newBuilder()
            .initialCapacity(50)
            .maximumSize(200)
            .expireAfterWrite(Duration.ofHours(6))
            .expireAfterAccess(Duration.ofHours(1))
            .recordStats()
            .build();
    }

    /**
     * 统计数据缓存
     */
    @Bean("statisticsCache")
    public Cache<String, Object> statisticsCache() {
        return Caffeine.newBuilder()
            .initialCapacity(50)
            .maximumSize(500)
            .expireAfterWrite(Duration.ofMinutes(30))
            .expireAfterAccess(Duration.ofMinutes(10))
            .recordStats()
            .build();
    }

    /**
     * 限流缓存
     */
    @Bean("rateLimitCache")
    public Cache<String, Object> rateLimitCache() {
        return Caffeine.newBuilder()
            .initialCapacity(1000)
            .maximumSize(10000)
            .expireAfterWrite(Duration.ofMinutes(1))
            .recordStats()
            .build();
    }
}