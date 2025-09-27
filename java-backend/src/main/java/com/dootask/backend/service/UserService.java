package com.dootask.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dootask.backend.entity.User;

import java.util.List;
import java.util.Map;

public interface UserService extends IService<User> {

    User login(String email, String password);

    User register(String email, String password, String nickname);

    String generateToken(User user);

    User getUserByToken(String token);

    User getUserById(Long userId);

    boolean validatePassword(String rawPassword, String encodedPassword);

    User userid2basic(Long userid, List<String> addField);

    List<User> searchUser(String keyword, Integer limit);

    List<User> getAllUsers(Integer page, Integer size);

    User updateUser(User user);

    void deleteUser(Long userId);

    void changeUserPassword(Long userId, String newPassword);

    void changeUserStatus(Long userId, String status);

    Map<String, Object> getUserStatistics();

    List<User> getUsersByRole(String role);

    void assignRole(Long userId, String role);

    void removeRole(Long userId, String role);

    List<User> getUsersByDepartment(Long departmentId);

    void assignDepartment(Long userId, Long departmentId);

    void resetUserPassword(Long userId, String defaultPassword);

    boolean isAdmin(Long userId);

    List<User> getActiveUsers();

    Map<String, Long> getUserCountByDepartment();

    List<Map<String, Object>> getUserRegistrationStats(int days);
}