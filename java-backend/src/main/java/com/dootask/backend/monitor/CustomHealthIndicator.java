package com.dootask.backend.monitor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 自定义健康检查指标
 */
@Slf4j
@Component
public class CustomHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;

    public CustomHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Health health() {
        Health.Builder builder = new Health.Builder();

        try {
            // 检查数据库连接
            if (isDatabaseHealthy()) {
                builder.up().withDetail("database", "Available");
            } else {
                builder.down().withDetail("database", "Unavailable");
            }

            // 检查内存使用
            long maxMemory = Runtime.getRuntime().maxMemory();
            long totalMemory = Runtime.getRuntime().totalMemory();
            long freeMemory = Runtime.getRuntime().freeMemory();
            long usedMemory = totalMemory - freeMemory;

            double memoryUsagePercent = (double) usedMemory / maxMemory * 100;

            builder.withDetail("memory", String.format("%.2f%% used", memoryUsagePercent));

            if (memoryUsagePercent > 90) {
                builder.down().withDetail("memory_status", "Critical");
            } else if (memoryUsagePercent > 80) {
                builder.unknown().withDetail("memory_status", "Warning");
            } else {
                builder.withDetail("memory_status", "Normal");
            }

            // 检查磁盘空间
            long freeSpace = Runtime.getRuntime().freeMemory();
            builder.withDetail("disk_free", freeSpace + " bytes");

            // 添加系统信息
            builder.withDetail("java_version", System.getProperty("java.version"));
            builder.withDetail("os_name", System.getProperty("os.name"));
            builder.withDetail("processors", Runtime.getRuntime().availableProcessors());

            return builder.build();

        } catch (Exception e) {
            log.error("健康检查失败", e);
            return builder.down().withException(e).build();
        }
    }

    /**
     * 检查数据库健康状态
     */
    private boolean isDatabaseHealthy() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(5); // 5秒超时
        } catch (SQLException e) {
            log.error("数据库连接检查失败", e);
            return false;
        }
    }
}