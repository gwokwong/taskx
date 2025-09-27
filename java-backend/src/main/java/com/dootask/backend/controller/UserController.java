package com.dootask.backend.controller;

import com.dootask.backend.common.Result;
import com.dootask.backend.entity.User;
import com.dootask.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

@Tag(name = "用户管理", description = "用户相关接口")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/profile")
    public Result<User> getProfile(HttpServletRequest request) {
        User user = getCurrentUser(request);
        return Result.success(user);
    }

    @Operation(summary = "更新用户信息")
    @PutMapping("/profile")
    public Result<User> updateProfile(@Valid @RequestBody UpdateProfileRequest request, HttpServletRequest httpRequest) {
        User currentUser = getCurrentUser(httpRequest);

        if (request.getNickname() != null) {
            currentUser.setNickname(request.getNickname());
        }
        if (request.getProfession() != null) {
            currentUser.setProfession(request.getProfession());
        }
        if (request.getTel() != null) {
            currentUser.setTel(request.getTel());
        }
        if (request.getUserImg() != null) {
            currentUser.setUserImg(request.getUserImg());
        }

        userService.updateById(currentUser);
        return Result.success("更新成功", currentUser);
    }

    @Operation(summary = "修改密码")
    @PostMapping("/change-password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request, HttpServletRequest httpRequest) {
        User currentUser = getCurrentUser(httpRequest);

        // 验证原密码
        if (!userService.validatePassword(request.getOldPassword(), currentUser.getPassword())) {
            return Result.error("原密码错误");
        }

        // 更新密码
        currentUser.setPassword(userService.passwordEncoder.encode(request.getNewPassword()));
        userService.updateById(currentUser);

        return Result.success("密码修改成功");
    }

    @Operation(summary = "搜索用户")
    @GetMapping("/search")
    public Result<List<User>> searchUsers(@RequestParam String keyword, @RequestParam(defaultValue = "20") Integer limit) {
        List<User> users = userService.searchUser(keyword, limit);
        return Result.success(users);
    }

    @Operation(summary = "获取用户基本信息")
    @GetMapping("/{userId}/basic")
    public Result<User> getUserBasic(@PathVariable Long userId) {
        User user = userService.userid2basic(userId, null);
        return Result.success(user);
    }

    private User getCurrentUser(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            User user = userService.getUserByToken(token);
            if (user == null) {
                throw new RuntimeException("用户未登录或token已失效");
            }
            return user;
        }
        throw new RuntimeException("请先登录");
    }

    public static class UpdateProfileRequest {
        private String nickname;
        private String profession;
        private String tel;
        private String userImg;

        // Getters and Setters
        public String getNickname() { return nickname; }
        public void setNickname(String nickname) { this.nickname = nickname; }
        public String getProfession() { return profession; }
        public void setProfession(String profession) { this.profession = profession; }
        public String getTel() { return tel; }
        public void setTel(String tel) { this.tel = tel; }
        public String getUserImg() { return userImg; }
        public void setUserImg(String userImg) { this.userImg = userImg; }
    }

    public static class ChangePasswordRequest {
        @NotBlank(message = "原密码不能为空")
        private String oldPassword;

        @NotBlank(message = "新密码不能为空")
        @Size(min = 6, message = "新密码不能少于6位")
        private String newPassword;

        // Getters and Setters
        public String getOldPassword() { return oldPassword; }
        public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }
}