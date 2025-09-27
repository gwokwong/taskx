package com.dootask.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dootask.backend.entity.UserSetting;

import java.util.List;
import java.util.Map;

public interface UserSettingService extends IService<UserSetting> {

    List<UserSetting> getUserSettings(Long userId);

    List<UserSetting> getUserSettingsByCategory(Long userId, String category);

    UserSetting getUserSetting(Long userId, String key);

    void updateUserSetting(Long userId, String key, String value);

    void updateUserSettings(Long userId, Map<String, String> settings);

    void initializeUserSettings(Long userId);
}