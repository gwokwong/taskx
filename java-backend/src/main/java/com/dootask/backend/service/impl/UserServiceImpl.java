package com.dootask.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dootask.backend.common.exception.ApiException;
import com.dootask.backend.entity.User;
import com.dootask.backend.mapper.UserMapper;
import com.dootask.backend.service.UserService;
import com.dootask.backend.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    public final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public User login(String email, String password) {
        if (!StringUtils.hasText(email) || !StringUtils.hasText(password)) {
            throw new ApiException("邮箱和密码不能为空");
        }

        User user = getOne(new QueryWrapper<User>().eq("email", email));
        if (user == null) {
            throw new ApiException("用户不存在");
        }

        if (user.getDisableAt() != null) {
            throw new ApiException("账号已停用");
        }

        if (!validatePassword(password, user.getPassword())) {
            throw new ApiException("密码错误");
        }

        // 更新登录信息
        user.setLoginNum(user.getLoginNum() + 1);
        user.setLastAt(LocalDateTime.now());
        user.setLineAt(LocalDateTime.now());
        updateById(user);

        return user;
    }

    @Override
    public User register(String email, String password, String nickname) {
        if (!StringUtils.hasText(email)) {
            throw new ApiException("请输入邮箱地址");
        }

        if (!isValidEmail(email)) {
            throw new ApiException("请输入正确的邮箱地址");
        }

        if (count(new QueryWrapper<User>().eq("email", email)) > 0) {
            throw new ApiException("邮箱地址已存在");
        }

        if (!StringUtils.hasText(password)) {
            throw new ApiException("请输入密码");
        }

        if (password.length() < 6) {
            throw new ApiException("密码不能少于6位");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(StringUtils.hasText(nickname) ? nickname : email.split("@")[0]);
        user.setEncrypt(UUID.randomUUID().toString().replace("-", ""));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        if (!save(user)) {
            throw new ApiException("注册失败");
        }

        return user;
    }

    @Override
    public String generateToken(User user) {
        return jwtUtil.generateToken(user.getUserid(), user.getEmail());
    }

    @Override
    public User getUserByToken(String token) {
        if (!StringUtils.hasText(token)) {
            return null;
        }

        if (!jwtUtil.validateToken(token)) {
            return null;
        }

        Long userId = jwtUtil.getUserIdFromToken(token);
        String email = jwtUtil.getEmailFromToken(token);

        if (userId == null || !StringUtils.hasText(email)) {
            return null;
        }

        return getOne(new QueryWrapper<User>()
                .eq("userid", userId)
                .eq("email", email));
    }

    @Override
    public User getUserById(Long userId) {
        if (userId == null || userId <= 0) {
            return null;
        }
        return getById(userId);
    }

    @Override
    public boolean validatePassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    @Override
    public User userid2basic(Long userid, List<String> addField) {
        if (userid == null || userid <= 0) {
            return null;
        }

        User user = getById(userid);
        if (user != null) {
            // 设置在线状态等扩展信息
            user.setOnline(true); // 简化实现，实际应该检查真实在线状态
            user.setDepartmentName(""); // 简化实现
        }
        return user;
    }

    @Override
    public List<User> searchUser(String keyword, Integer limit) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();

        if (keyword.contains("@")) {
            queryWrapper.like("email", keyword);
        } else {
            queryWrapper.and(wrapper -> wrapper
                    .like("nickname", keyword)
                    .or()
                    .like("pinyin", keyword)
                    .or()
                    .like("profession", keyword));
        }

        queryWrapper.orderByAsc("userid").last("LIMIT " + (limit != null ? limit : 20));

        return list(queryWrapper);
    }

    @Override
    public List<User> getAllUsers(Integer page, Integer size) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("created_at");

        if (page != null && size != null) {
            Page<User> pageInfo = new Page<>(page, size);
            Page<User> result = page(pageInfo, queryWrapper);
            return result.getRecords();
        } else {
            return list(queryWrapper);
        }
    }

    @Override
    public User updateUser(User user) {
        if (user.getUserid() == null) {
            throw new ApiException("用户ID不能为空");
        }

        User existingUser = getById(user.getUserid());
        if (existingUser == null) {
            throw new ApiException("用户不存在");
        }

        user.setUpdatedAt(LocalDateTime.now());
        updateById(user);
        return user;
    }

    @Override
    public void deleteUser(Long userId) {
        if (userId == null || userId <= 0) {
            throw new ApiException("用户ID不能为空");
        }

        User user = getById(userId);
        if (user == null) {
            throw new ApiException("用户不存在");
        }

        if (isAdmin(userId)) {
            throw new ApiException("不能删除管理员账户");
        }

        removeById(userId);
    }

    @Override
    public void changeUserPassword(Long userId, String newPassword) {
        if (userId == null || userId <= 0) {
            throw new ApiException("用户ID不能为空");
        }

        if (!StringUtils.hasText(newPassword)) {
            throw new ApiException("新密码不能为空");
        }

        if (newPassword.length() < 6) {
            throw new ApiException("密码不能少于6位");
        }

        User user = getById(userId);
        if (user == null) {
            throw new ApiException("用户不存在");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        updateById(user);
    }

    @Override
    public void changeUserStatus(Long userId, String status) {
        if (userId == null || userId <= 0) {
            throw new ApiException("用户ID不能为空");
        }

        User user = getById(userId);
        if (user == null) {
            throw new ApiException("用户不存在");
        }

        if ("disabled".equals(status)) {
            user.setDisableAt(LocalDateTime.now());
        } else {
            user.setDisableAt(null);
        }

        user.setUpdatedAt(LocalDateTime.now());
        updateById(user);
    }

    @Override
    public Map<String, Object> getUserStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // 总用户数
        long totalUsers = count();
        stats.put("totalUsers", totalUsers);

        // 活跃用户数（最近30天登录）
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        long activeUsers = count(new QueryWrapper<User>().gt("last_at", thirtyDaysAgo));
        stats.put("activeUsers", activeUsers);

        // 停用用户数
        long disabledUsers = count(new QueryWrapper<User>().isNotNull("disable_at"));
        stats.put("disabledUsers", disabledUsers);

        // 本月新注册用户数
        LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        long newUsersThisMonth = count(new QueryWrapper<User>().gt("created_at", monthStart));
        stats.put("newUsersThisMonth", newUsersThisMonth);

        return stats;
    }

    @Override
    public List<User> getUsersByRole(String role) {
        // 简化实现，实际应该关联角色表
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("role", role);
        return list(queryWrapper);
    }

    @Override
    public void assignRole(Long userId, String role) {
        if (userId == null || userId <= 0) {
            throw new ApiException("用户ID不能为空");
        }

        User user = getById(userId);
        if (user == null) {
            throw new ApiException("用户不存在");
        }

        // 简化实现，实际应该操作用户角色关联表
        user.setRole(role);
        user.setUpdatedAt(LocalDateTime.now());
        updateById(user);
    }

    @Override
    public void removeRole(Long userId, String role) {
        if (userId == null || userId <= 0) {
            throw new ApiException("用户ID不能为空");
        }

        User user = getById(userId);
        if (user == null) {
            throw new ApiException("用户不存在");
        }

        // 简化实现
        if (role.equals(user.getRole())) {
            user.setRole(null);
            user.setUpdatedAt(LocalDateTime.now());
            updateById(user);
        }
    }

    @Override
    public List<User> getUsersByDepartment(Long departmentId) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("department_id", departmentId);
        return list(queryWrapper);
    }

    @Override
    public void assignDepartment(Long userId, Long departmentId) {
        if (userId == null || userId <= 0) {
            throw new ApiException("用户ID不能为空");
        }

        User user = getById(userId);
        if (user == null) {
            throw new ApiException("用户不存在");
        }

        user.setDepartmentId(departmentId);
        user.setUpdatedAt(LocalDateTime.now());
        updateById(user);
    }

    @Override
    public void resetUserPassword(Long userId, String defaultPassword) {
        if (userId == null || userId <= 0) {
            throw new ApiException("用户ID不能为空");
        }

        String password = StringUtils.hasText(defaultPassword) ? defaultPassword : "123456";
        changeUserPassword(userId, password);
    }

    @Override
    public boolean isAdmin(Long userId) {
        if (userId == null || userId <= 0) {
            return false;
        }

        User user = getById(userId);
        if (user == null) {
            return false;
        }

        // 简化实现，实际应该检查用户角色
        return "admin".equals(user.getRole()) || userId == 1L;
    }

    @Override
    public List<User> getActiveUsers() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.gt("last_at", thirtyDaysAgo)
                .isNull("disable_at")
                .orderByDesc("last_at");
        return list(queryWrapper);
    }

    @Override
    public Map<String, Long> getUserCountByDepartment() {
        // 简化实现，实际应该通过mapper查询
        List<Map<String, Object>> stats = baseMapper.getUserCountByDepartment();
        Map<String, Long> result = new HashMap<>();
        for (Map<String, Object> stat : stats) {
            String departmentName = (String) stat.get("department_name");
            Long count = ((Number) stat.get("count")).longValue();
            result.put(departmentName != null ? departmentName : "未分配", count);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getUserRegistrationStats(int days) {
        return baseMapper.getUserRegistrationStats(days);
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
}