package com.dootask.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dootask.backend.entity.SystemSetting;

import java.util.List;
import java.util.Map;

public interface SystemSettingService extends IService<SystemSetting> {

    List<SystemSetting> getPublicSettings();

    List<SystemSetting> getSettingsByCategory(String category);

    SystemSetting getSettingByKey(String key);

    void updateSetting(String key, String value);

    void updateSettings(Map<String, String> settings);

    Map<String, Object> getSystemInfo();

    void initializeDefaultSettings();
}