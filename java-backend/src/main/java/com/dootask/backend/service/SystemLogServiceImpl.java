package com.dootask.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dootask.backend.entity.SystemLog;
import com.dootask.backend.mapper.SystemLogMapper;
import com.dootask.backend.service.SystemLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SystemLogServiceImpl extends ServiceImpl<SystemLogMapper, SystemLog> implements SystemLogService {

    @Override
    public void logAction(String action, String module, String description, Long userId, String ipAddress, String userAgent) {
        logAction(action, module, description, userId, ipAddress, userAgent, null, null, null, null, null);
    }

    @Override
    public void logAction(String action, String module, String description, Long userId, String ipAddress, String userAgent, String requestPath, String requestMethod, String requestParams, String responseStatus, Long executionTime) {
        SystemLog log = new SystemLog();
        log.setUserId(userId);
        log.setAction(action);
        log.setModule(module);
        log.setDescription(description);
        log.setIpAddress(ipAddress);
        log.setUserAgent(userAgent);
        log.setRequestPath(requestPath);
        log.setRequestMethod(requestMethod);
        log.setRequestParams(requestParams);
        log.setResponseStatus(responseStatus);
        log.setExecutionTime(executionTime);
        log.setCreatedAt(LocalDateTime.now());

        save(log);
    }

    @Override
    public List<SystemLog> getLogs(String action, String module, LocalDateTime startTime, LocalDateTime endTime, Integer page, Integer size) {
        QueryWrapper<SystemLog> queryWrapper = new QueryWrapper<>();

        if (action != null && !action.isEmpty()) {
            queryWrapper.eq("action", action);
        }

        if (module != null && !module.isEmpty()) {
            queryWrapper.eq("module", module);
        }

        if (startTime != null) {
            queryWrapper.ge("created_at", startTime);
        }

        if (endTime != null) {
            queryWrapper.le("created_at", endTime);
        }

        queryWrapper.orderByDesc("created_at");

        if (page != null && size != null) {
            Page<SystemLog> pageInfo = new Page<>(page, size);
            Page<SystemLog> result = page(pageInfo, queryWrapper);
            return result.getRecords();
        } else {
            return list(queryWrapper);
        }
    }

    @Override
    public Map<String, Long> getActionStatistics() {
        List<Map<String, Object>> stats = baseMapper.getActionStatistics();
        return stats.stream().collect(Collectors.toMap(
                map -> (String) map.get("action"),
                map -> ((Number) map.get("count")).longValue()
        ));
    }

    @Override
    public Map<String, Long> getModuleStatistics() {
        List<Map<String, Object>> stats = baseMapper.getModuleStatistics();
        return stats.stream().collect(Collectors.toMap(
                map -> (String) map.get("module"),
                map -> ((Number) map.get("count")).longValue()
        ));
    }

    @Override
    public List<Map<String, Object>> getDailyLogStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        return baseMapper.getDailyLogStatistics(startDate, endDate);
    }

    @Override
    public void cleanOldLogs(Integer daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        remove(new QueryWrapper<SystemLog>().lt("created_at", cutoffDate));
    }
}