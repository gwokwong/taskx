package com.dootask.backend.controller;

import com.dootask.backend.common.Result;
import com.dootask.backend.entity.UserSetting;
import com.dootask.backend.service.UserSettingService;
import com.dootask.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Tag(name = "用户设置", description = "用户设置相关接口")
@RestController
@RequestMapping("/api/user/settings")
@RequiredArgsConstructor
public class UserSettingController {

    private final UserSettingService userSettingService;
    private final UserService userService;

    @Operation(summary = "获取用户设置")
    @GetMapping
    public Result<List<UserSetting>> getUserSettings(
            @RequestParam(required = false) String category,
            HttpServletRequest request) {

        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        List<UserSetting> settings;
        if (category != null) {
            settings = userSettingService.getUserSettingsByCategory(userId, category);
        } else {
            settings = userSettingService.getUserSettings(userId);
        }

        return Result.success(settings);
    }

    @Operation(summary = "获取单个用户设置")
    @GetMapping("/{key}")
    public Result<UserSetting> getUserSetting(@PathVariable String key,
                                             HttpServletRequest request) {

        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        UserSetting setting = userSettingService.getUserSetting(userId, key);
        return Result.success(setting);
    }

    @Operation(summary = "更新用户设置")
    @PutMapping
    public Result<Void> updateUserSettings(@RequestBody Map<String, String> settings,
                                          HttpServletRequest request) {

        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        userSettingService.updateUserSettings(userId, settings);
        return Result.success("设置更新成功");
    }

    @Operation(summary = "更新单个用户设置")
    @PutMapping("/{key}")
    public Result<Void> updateUserSetting(@PathVariable String key,
                                         @RequestBody Map<String, String> data,
                                         HttpServletRequest request) {

        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        String value = data.get("value");
        userSettingService.updateUserSetting(userId, key, value);
        return Result.success("设置更新成功");
    }

    @Operation(summary = "初始化用户默认设置")
    @PostMapping("/initialize")
    public Result<Void> initializeUserSettings(HttpServletRequest request) {

        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        userSettingService.initializeUserSettings(userId);
        return Result.success("初始化成功");
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