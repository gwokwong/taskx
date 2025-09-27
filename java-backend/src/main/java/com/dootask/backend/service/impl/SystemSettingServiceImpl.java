package com.dootask.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dootask.backend.entity.SystemSetting;
import com.dootask.backend.mapper.SystemSettingMapper;
import com.dootask.backend.service.SystemSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SystemSettingServiceImpl extends ServiceImpl<SystemSettingMapper, SystemSetting> implements SystemSettingService {

    @Override
    public List<SystemSetting> getPublicSettings() {
        return list(new QueryWrapper<SystemSetting>()
                .eq("is_public", true));
    }

    @Override
    public List<SystemSetting> getSettingsByCategory(String category) {
        return list(new QueryWrapper<SystemSetting>()
                .eq("category", category));
    }

    @Override
    public SystemSetting getSettingByKey(String key) {
        return getOne(new QueryWrapper<SystemSetting>()
                .eq("setting_key", key));
    }

    @Override
    public void updateSetting(String key, String value) {
        SystemSetting setting = getSettingByKey(key);
        if (setting != null) {
            setting.setSettingValue(value);
            setting.setUpdatedAt(LocalDateTime.now());
            updateById(setting);
        } else {
            // Create new setting if not exists
            setting = new SystemSetting();
            setting.setSettingKey(key);
            setting.setSettingValue(value);
            setting.setSettingType("string");
            setting.setCategory("general");
            setting.setIsPublic(false);
            setting.setCreatedAt(LocalDateTime.now());
            setting.setUpdatedAt(LocalDateTime.now());
            save(setting);
        }
    }

    @Override
    public void updateSettings(Map<String, String> settings) {
        for (Map.Entry<String, String> entry : settings.entrySet()) {
            updateSetting(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public Map<String, Object> getSystemInfo() {
        Map<String, Object> systemInfo = new HashMap<>();

        // Basic system information
        systemInfo.put("applicationName", "DooTask");
        systemInfo.put("version", "1.0.0");
        systemInfo.put("javaVersion", System.getProperty("java.version"));
        systemInfo.put("osName", System.getProperty("os.name"));
        systemInfo.put("osVersion", System.getProperty("os.version"));

        // Runtime information
        Runtime runtime = Runtime.getRuntime();
        systemInfo.put("totalMemory", runtime.totalMemory());
        systemInfo.put("freeMemory", runtime.freeMemory());
        systemInfo.put("maxMemory", runtime.maxMemory());
        systemInfo.put("availableProcessors", runtime.availableProcessors());

        return systemInfo;
    }

    @PostConstruct
    @Override
    public void initializeDefaultSettings() {
        if (count() == 0) {
            createDefaultSetting("app.name", "DooTask", "string", "general", "应用程序名称", true);
            createDefaultSetting("app.version", "1.0.0", "string", "general", "应用程序版本", true);
            createDefaultSetting("app.logo", "/images/logo.png", "string", "general", "应用程序Logo", true);
            createDefaultSetting("app.timezone", "Asia/Shanghai", "string", "general", "默认时区", true);
            createDefaultSetting("app.language", "zh-CN", "string", "general", "默认语言", true);

            // Email settings
            createDefaultSetting("email.smtp.host", "smtp.gmail.com", "string", "email", "SMTP服务器地址", false);
            createDefaultSetting("email.smtp.port", "587", "number", "email", "SMTP端口", false);
            createDefaultSetting("email.smtp.username", "", "string", "email", "SMTP用户名", false);
            createDefaultSetting("email.smtp.password", "", "string", "email", "SMTP密码", false);
            createDefaultSetting("email.smtp.auth", "true", "boolean", "email", "是否启用SMTP认证", false);
            createDefaultSetting("email.smtp.starttls", "true", "boolean", "email", "是否启用STARTTLS", false);

            // Storage settings
            createDefaultSetting("storage.type", "local", "string", "storage", "存储类型", false);
            createDefaultSetting("storage.local.path", "/uploads", "string", "storage", "本地存储路径", false);
            createDefaultSetting("storage.max.file.size", "10485760", "number", "storage", "最大文件大小(字节)", false);

            // Security settings
            createDefaultSetting("security.jwt.secret", "dootask-secret-key", "string", "security", "JWT密钥", false);
            createDefaultSetting("security.jwt.expiration", "86400", "number", "security", "JWT过期时间(秒)", false);
            createDefaultSetting("security.password.min.length", "6", "number", "security", "密码最小长度", false);
            createDefaultSetting("security.login.max.attempts", "5", "number", "security", "最大登录尝试次数", false);
        }
    }

    private void createDefaultSetting(String key, String value, String type, String category, String description, Boolean isPublic) {
        SystemSetting setting = new SystemSetting();
        setting.setSettingKey(key);
        setting.setSettingValue(value);
        setting.setSettingType(type);
        setting.setCategory(category);
        setting.setDescription(description);
        setting.setIsPublic(isPublic);
        setting.setCreatedAt(LocalDateTime.now());
        setting.setUpdatedAt(LocalDateTime.now());
        save(setting);
    }
}