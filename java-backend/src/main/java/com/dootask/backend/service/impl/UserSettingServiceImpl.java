package com.dootask.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dootask.backend.entity.UserSetting;
import com.dootask.backend.mapper.UserSettingMapper;
import com.dootask.backend.service.UserSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserSettingServiceImpl extends ServiceImpl<UserSettingMapper, UserSetting> implements UserSettingService {

    @Override
    public List<UserSetting> getUserSettings(Long userId) {
        return list(new QueryWrapper<UserSetting>()
                .eq("user_id", userId));
    }

    @Override
    public List<UserSetting> getUserSettingsByCategory(Long userId, String category) {
        return list(new QueryWrapper<UserSetting>()
                .eq("user_id", userId)
                .eq("category", category));
    }

    @Override
    public UserSetting getUserSetting(Long userId, String key) {
        return getOne(new QueryWrapper<UserSetting>()
                .eq("user_id", userId)
                .eq("setting_key", key));
    }

    @Override
    public void updateUserSetting(Long userId, String key, String value) {
        UserSetting setting = getUserSetting(userId, key);
        if (setting != null) {
            setting.setSettingValue(value);
            setting.setUpdatedAt(LocalDateTime.now());
            updateById(setting);
        } else {
            // Create new setting if not exists
            setting = new UserSetting();
            setting.setUserId(userId);
            setting.setSettingKey(key);
            setting.setSettingValue(value);
            setting.setSettingType("string");
            setting.setCategory("general");
            setting.setCreatedAt(LocalDateTime.now());
            setting.setUpdatedAt(LocalDateTime.now());
            save(setting);
        }
    }

    @Override
    public void updateUserSettings(Long userId, Map<String, String> settings) {
        for (Map.Entry<String, String> entry : settings.entrySet()) {
            updateUserSetting(userId, entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void initializeUserSettings(Long userId) {
        // Check if user already has settings
        if (count(new QueryWrapper<UserSetting>().eq("user_id", userId)) == 0) {
            createDefaultUserSetting(userId, "theme", "light", "string", "theme");
            createDefaultUserSetting(userId, "language", "zh-CN", "string", "language");
            createDefaultUserSetting(userId, "timezone", "Asia/Shanghai", "string", "general");
            createDefaultUserSetting(userId, "notification.email", "true", "boolean", "notification");
            createDefaultUserSetting(userId, "notification.desktop", "true", "boolean", "notification");
            createDefaultUserSetting(userId, "notification.sound", "true", "boolean", "notification");
            createDefaultUserSetting(userId, "privacy.profile.public", "false", "boolean", "privacy");
            createDefaultUserSetting(userId, "privacy.activity.visible", "true", "boolean", "privacy");
        }
    }

    private void createDefaultUserSetting(Long userId, String key, String value, String type, String category) {
        UserSetting setting = new UserSetting();
        setting.setUserId(userId);
        setting.setSettingKey(key);
        setting.setSettingValue(value);
        setting.setSettingType(type);
        setting.setCategory(category);
        setting.setCreatedAt(LocalDateTime.now());
        setting.setUpdatedAt(LocalDateTime.now());
        save(setting);
    }
}