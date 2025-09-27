package com.dootask.backend.controller;

import com.dootask.backend.common.Result;
import com.dootask.backend.entity.SystemSetting;
import com.dootask.backend.service.SystemSettingService;
import com.dootask.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Tag(name = "系统设置", description = "系统设置相关接口")
@RestController
@RequestMapping("/api/system")
@RequiredArgsConstructor
public class SystemController {

    private final SystemSettingService systemSettingService;
    private final UserService userService;

    @Operation(summary = "获取公开系统设置")
    @GetMapping("/settings/public")
    public Result<List<SystemSetting>> getPublicSettings() {
        List<SystemSetting> settings = systemSettingService.getPublicSettings();
        return Result.success(settings);
    }

    @Operation(summary = "获取系统信息")
    @GetMapping("/info")
    public Result<Map<String, Object>> getSystemInfo() {
        Map<String, Object> info = systemSettingService.getSystemInfo();
        return Result.success(info);
    }

    @Operation(summary = "获取系统设置(管理员)")
    @GetMapping("/settings")
    public Result<List<SystemSetting>> getSystemSettings(
            @RequestParam(required = false) String category,
            HttpServletRequest request) {

        // TODO: 验证管理员权限
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        List<SystemSetting> settings;
        if (category != null) {
            settings = systemSettingService.getSettingsByCategory(category);
        } else {
            settings = systemSettingService.list();
        }

        return Result.success(settings);
    }

    @Operation(summary = "更新系统设置(管理员)")
    @PutMapping("/settings")
    public Result<Void> updateSystemSettings(@RequestBody Map<String, String> settings,
                                            HttpServletRequest request) {

        // TODO: 验证管理员权限
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        systemSettingService.updateSettings(settings);
        return Result.success("设置更新成功");
    }

    @Operation(summary = "更新单个系统设置(管理员)")
    @PutMapping("/settings/{key}")
    public Result<Void> updateSystemSetting(@PathVariable String key,
                                           @RequestBody Map<String, String> data,
                                           HttpServletRequest request) {

        // TODO: 验证管理员权限
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        String value = data.get("value");
        systemSettingService.updateSetting(key, value);
        return Result.success("设置更新成功");
    }

    private Long getCurrentUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            var user = userService.getUserByToken(token);
            return user != null ? user.getUserid() : null;
        }
        return null;
    }
}