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
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import com.dootask.backend.entity.Department;
import com.dootask.backend.entity.UserDepartment;
import java.time.LocalDateTime;

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
    public Result<String> changePassword(@Valid @RequestBody ChangePasswordRequest request, HttpServletRequest httpRequest) {
        User currentUser = getCurrentUser(httpRequest);

        // 验证原密码
        if (!userService.validatePassword(request.getOldPassword(), currentUser.getPassword())) {
            return Result.error("原密码错误");
        }

        // 更新密码
        currentUser.setPassword(userService.encodePassword(request.getNewPassword()));
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

    @Operation(summary = "获取用户列表")
    @GetMapping("/list")
    public Result<Page<User>> getUserList(@RequestParam(required = false) String keyword,
                                          @RequestParam(required = false) Long departmentId,
                                          @RequestParam(required = false) String status,
                                          Pageable pageable, HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        Page<User> users = userService.getUserList(keyword, departmentId, status, pageable, currentUserId);
        return Result.success(users);
    }

    @Operation(summary = "创建用户")
    @PostMapping
    public Result<User> createUser(@Valid @RequestBody CreateUserRequest request, HttpServletRequest httpRequest) {
        Long currentUserId = getCurrentUserId(httpRequest);
        User user = userService.createUser(request, currentUserId);
        return Result.success("用户创建成功", user);
    }

    @Operation(summary = "更新用户")
    @PutMapping("/{userId}")
    public Result<User> updateUser(@PathVariable Long userId, @Valid @RequestBody UpdateUserRequest request, HttpServletRequest httpRequest) {
        Long currentUserId = getCurrentUserId(httpRequest);
        User user = userService.updateUser(userId, request, currentUserId);
        return Result.success("用户更新成功", user);
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{userId}")
    public Result<String> deleteUser(@PathVariable Long userId, HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        userService.deleteUser(userId, currentUserId);
        return Result.success("用户删除成功");
    }

    @Operation(summary = "禁用/启用用户")
    @PostMapping("/{userId}/toggle-status")
    public Result<String> toggleUserStatus(@PathVariable Long userId, HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        boolean isEnabled = userService.toggleUserStatus(userId, currentUserId);
        return Result.success(isEnabled ? "用户已启用" : "用户已禁用");
    }

    @Operation(summary = "重置用户密码")
    @PostMapping("/{userId}/reset-password")
    public Result<String> resetUserPassword(@PathVariable Long userId, @RequestBody ResetPasswordRequest request, HttpServletRequest httpRequest) {
        Long currentUserId = getCurrentUserId(httpRequest);
        String newPassword = userService.resetUserPassword(userId, request.getNewPassword(), currentUserId);
        return Result.success("密码重置成功", newPassword);
    }

    @Operation(summary = "上传用户头像")
    @PostMapping("/upload-avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        User currentUser = getCurrentUser(request);
        String avatarUrl = userService.uploadAvatar(file, currentUser.getUserid());
        return Result.success("头像上传成功", avatarUrl);
    }

    @Operation(summary = "获取用户部门")
    @GetMapping("/{userId}/departments")
    public Result<List<Department>> getUserDepartments(@PathVariable Long userId, HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        List<Department> departments = userService.getUserDepartments(userId, currentUserId);
        return Result.success(departments);
    }

    @Operation(summary = "设置用户部门")
    @PostMapping("/{userId}/departments")
    public Result<String> setUserDepartments(@PathVariable Long userId, @RequestBody SetDepartmentsRequest request, HttpServletRequest httpRequest) {
        Long currentUserId = getCurrentUserId(httpRequest);
        userService.setUserDepartments(userId, request.getDepartmentIds(), currentUserId);
        return Result.success("部门设置成功");
    }

    @Operation(summary = "获取用户统计信息")
    @GetMapping("/{userId}/statistics")
    public Result<Map<String, Object>> getUserStatistics(@PathVariable Long userId, HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        Map<String, Object> statistics = userService.getUserStatistics(userId, currentUserId);
        return Result.success(statistics);
    }

    @Operation(summary = "获取用户操作日志")
    @GetMapping("/{userId}/logs")
    public Result<Page<Map<String, Object>>> getUserLogs(@PathVariable Long userId, Pageable pageable, HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        Page<Map<String, Object>> logs = userService.getUserLogs(userId, pageable, currentUserId);
        return Result.success(logs);
    }

    @Operation(summary = "批量导入用户")
    @PostMapping("/batch-import")
    public Result<Map<String, Object>> batchImportUsers(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        Map<String, Object> result = userService.batchImportUsers(file, currentUserId);
        return Result.success("批量导入完成", result);
    }

    @Operation(summary = "导出用户数据")
    @GetMapping("/export")
    public Result<String> exportUsers(@RequestParam(required = false) String keyword,
                                      @RequestParam(required = false) Long departmentId,
                                      @RequestParam(required = false) String status,
                                      HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        String downloadUrl = userService.exportUsers(keyword, departmentId, status, currentUserId);
        return Result.success("导出任务已创建", downloadUrl);
    }

    @Operation(summary = "获取在线用户")
    @GetMapping("/online")
    public Result<List<User>> getOnlineUsers(HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        List<User> users = userService.getOnlineUsers(currentUserId);
        return Result.success(users);
    }

    @Operation(summary = "发送系统通知")
    @PostMapping("/send-notification")
    public Result<String> sendNotification(@RequestBody SendNotificationRequest request, HttpServletRequest httpRequest) {
        Long currentUserId = getCurrentUserId(httpRequest);
        userService.sendNotification(request.getUserIds(), request.getTitle(), request.getContent(), currentUserId);
        return Result.success("通知发送成功");
    }

    @Operation(summary = "获取用户设置")
    @GetMapping("/settings")
    public Result<Map<String, Object>> getUserSettings(HttpServletRequest request) {
        User currentUser = getCurrentUser(request);
        Map<String, Object> settings = userService.getUserSettings(currentUser.getUserid());
        return Result.success(settings);
    }

    @Operation(summary = "更新用户设置")
    @PutMapping("/settings")
    public Result<String> updateUserSettings(@RequestBody Map<String, Object> settings, HttpServletRequest request) {
        User currentUser = getCurrentUser(request);
        userService.updateUserSettings(currentUser.getUserid(), settings);
        return Result.success("设置更新成功");
    }

    @Operation(summary = "绑定二步验证")
    @PostMapping("/bind-2fa")
    public Result<Map<String, Object>> bind2FA(HttpServletRequest request) {
        User currentUser = getCurrentUser(request);
        Map<String, Object> result = userService.bind2FA(currentUser.getUserid());
        return Result.success("二步验证绑定成功", result);
    }

    @Operation(summary = "验证二步验证")
    @PostMapping("/verify-2fa")
    public Result<String> verify2FA(@RequestBody Verify2FARequest request, HttpServletRequest httpRequest) {
        User currentUser = getCurrentUser(httpRequest);
        boolean isValid = userService.verify2FA(currentUser.getUserid(), request.getCode());
        return isValid ? Result.success("验证成功") : Result.error("验证码错误");
    }

    @Operation(summary = "解绑二步验证")
    @PostMapping("/unbind-2fa")
    public Result<String> unbind2FA(@RequestBody Verify2FARequest request, HttpServletRequest httpRequest) {
        User currentUser = getCurrentUser(httpRequest);
        boolean success = userService.unbind2FA(currentUser.getUserid(), request.getCode());
        return success ? Result.success("解绑成功") : Result.error("验证码错误");
    }

    private Long getCurrentUserId(HttpServletRequest request) {
        User user = getCurrentUser(request);
        return user.getUserid();
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

        public String getOldPassword() { return oldPassword; }
        public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }

    public static class CreateUserRequest {
        @NotBlank(message = "用户名不能为空")
        private String username;
        @NotBlank(message = "姓名不能为空")
        private String nickname;
        @NotBlank(message = "邮箱不能为空")
        private String email;
        private String password;
        private String tel;
        private String profession;
        private List<Long> departmentIds;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getNickname() { return nickname; }
        public void setNickname(String nickname) { this.nickname = nickname; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getTel() { return tel; }
        public void setTel(String tel) { this.tel = tel; }
        public String getProfession() { return profession; }
        public void setProfession(String profession) { this.profession = profession; }
        public List<Long> getDepartmentIds() { return departmentIds; }
        public void setDepartmentIds(List<Long> departmentIds) { this.departmentIds = departmentIds; }
    }

    public static class UpdateUserRequest {
        private String nickname;
        private String email;
        private String tel;
        private String profession;
        private String status;
        private List<Long> departmentIds;

        public String getNickname() { return nickname; }
        public void setNickname(String nickname) { this.nickname = nickname; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getTel() { return tel; }
        public void setTel(String tel) { this.tel = tel; }
        public String getProfession() { return profession; }
        public void setProfession(String profession) { this.profession = profession; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public List<Long> getDepartmentIds() { return departmentIds; }
        public void setDepartmentIds(List<Long> departmentIds) { this.departmentIds = departmentIds; }
    }

    public static class ResetPasswordRequest {
        private String newPassword;

        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }

    public static class SetDepartmentsRequest {
        private List<Long> departmentIds;

        public List<Long> getDepartmentIds() { return departmentIds; }
        public void setDepartmentIds(List<Long> departmentIds) { this.departmentIds = departmentIds; }
    }

    public static class SendNotificationRequest {
        private List<Long> userIds;
        private String title;
        private String content;

        public List<Long> getUserIds() { return userIds; }
        public void setUserIds(List<Long> userIds) { this.userIds = userIds; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }

    public static class Verify2FARequest {
        @NotBlank(message = "验证码不能为空")
        private String code;

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
    }
}