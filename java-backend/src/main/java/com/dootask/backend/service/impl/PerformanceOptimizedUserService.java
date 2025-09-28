package com.dootask.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dootask.backend.entity.User;
import com.dootask.backend.mapper.UserMapper;
import com.dootask.backend.service.UserService;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
// Temporarily disabled
// import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 用户服务实现类 - 性能优化版本
 */
@Slf4j
// @Service
public class PerformanceOptimizedUserService extends ServiceImpl<UserMapper, User> implements UserService {

    private final Cache<String, Object> permissionCache;
    private final Cache<String, Object> sessionCache;

    public PerformanceOptimizedUserService(Cache<String, Object> permissionCache, Cache<String, Object> sessionCache) {
        this.permissionCache = permissionCache;
        this.sessionCache = sessionCache;
    }

    @Override
    @Cacheable(value = "users", key = "#id")
    public User getUserById(Long id) {
        log.debug("从数据库获取用户信息: {}", id);
        return baseMapper.selectById(id);
    }

    @Cacheable(value = "users", key = "'email:' + #email")
    public User getUserByEmail(String email) {
        log.debug("从数据库获取用户信息: {}", email);
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getEmail, email);
        return baseMapper.selectOne(queryWrapper);
    }

    @Override
    @Cacheable(value = "users", key = "'list:' + #page + ':' + #size")
    public List<User> getAllUsers(Integer page, Integer size) {
        log.debug("从数据库获取用户列表: page={}, size={}", page, size);

        if (page != null && size != null) {
            // 使用分页查询
            Page<User> pageInfo = new Page<>(page, size);
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.orderByDesc(User::getCreatedAt);
            return baseMapper.selectPage(pageInfo, queryWrapper).getRecords();
        }

        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(User::getCreatedAt);
        return baseMapper.selectList(queryWrapper);
    }

    @CacheEvict(value = "users", allEntries = true)
    public User createUser(User user) {
        log.info("创建用户: {}", user.getEmail());

        // 设置默认值
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        baseMapper.insert(user);

        // 清除相关缓存
        clearUserRelatedCache(user.getUserid());

        return user;
    }

    @Override
    @CacheEvict(value = "users", key = "#user.userid")
    public User updateUser(User user) {
        log.info("更新用户: {}", user.getUserid());

        user.setUpdatedAt(LocalDateTime.now());
        baseMapper.updateById(user);

        // 清除相关缓存
        clearUserRelatedCache(user.getUserid());

        return user;
    }

    @Override
    @CacheEvict(value = "users", key = "#id")
    public void deleteUser(Long id) {
        log.info("删除用户: {}", id);

        baseMapper.deleteById(id);

        // 清除相关缓存
        clearUserRelatedCache(id);
    }

    @Override
    public boolean isAdmin(Long userId) {
        String cacheKey = "admin:" + userId;

        // 首先检查缓存
        Boolean isAdmin = (Boolean) permissionCache.getIfPresent(cacheKey);
        if (isAdmin != null) {
            return isAdmin;
        }

        // 从数据库查询
        User user = getUserById(userId);
        boolean admin = user != null && "admin".equals(user.getRole());

        // 缓存结果
        permissionCache.put(cacheKey, admin);

        return admin;
    }

    @Override
    public User getUserByToken(String token) {
        String cacheKey = "token:" + token;

        // 首先检查会话缓存
        User user = (User) sessionCache.getIfPresent(cacheKey);
        if (user != null) {
            return user;
        }

        // 从数据库查询 token 关联的用户
        // 这里需要实现 token 到用户的映射逻辑
        // 暂时返回 null，实际项目中需要根据 JWT 或其他 token 机制实现
        return null;
    }

    /**
     * 批量获取用户信息 - 使用缓存优化
     */
    public List<User> getUsersByIds(List<Long> userIds) {
        List<User> users = new ArrayList<>();
        List<Long> uncachedIds = new ArrayList<>();

        // 首先从缓存获取
        for (Long id : userIds) {
            User cachedUser = (User) permissionCache.getIfPresent("user:" + id);
            if (cachedUser != null) {
                users.add(cachedUser);
            } else {
                uncachedIds.add(id);
            }
        }

        // 批量查询未缓存的用户
        if (!uncachedIds.isEmpty()) {
            List<User> dbUsers = baseMapper.selectBatchIds(uncachedIds);
            for (User user : dbUsers) {
                permissionCache.put("user:" + user.getUserid(), user);
                users.add(user);
            }
        }

        return users;
    }

    /**
     * 预热缓存
     */
    public void warmUpCache() {
        log.info("开始预热用户缓存");

        try {
            // 预加载活跃用户
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.isNull(User::getDisableAt)
                    .ge(User::getLastAt, LocalDateTime.now().minusDays(7))
                    .orderByDesc(User::getLastAt)
                    .last("LIMIT 100");
            List<User> activeUsers = baseMapper.selectList(queryWrapper);

            for (User user : activeUsers) {
                permissionCache.put("user:" + user.getUserid(), user);
                permissionCache.put("admin:" + user.getUserid(), "admin".equals(user.getRole()));
            }

            log.info("用户缓存预热完成，预加载 {} 个活跃用户", activeUsers.size());

        } catch (Exception e) {
            log.error("用户缓存预热失败", e);
        }
    }

    @Override
    public List<Map<String, Object>> getUserRegistrationStats(int days) {
        // 暂时返回空列表，实际项目中需要实现具体的统计逻辑
        return new java.util.ArrayList<>();
    }

    @Override
    public String encodePassword(String rawPassword) {
        // 使用简单的加密，实际项目中应使用 BCrypt 等安全的加密方式
        return String.valueOf(rawPassword.hashCode());
    }

    // 方法签名错误，正确的方法名应该是 login
    @Override
    public User login(String email, String password) {
        // 这里需要实现登录逻辑
        return null;
    }

    @Override
    public User register(String email, String password, String nickname) {
        // 这里需要实现注册逻辑
        return null;
    }

    @Override
    public String generateToken(User user) {
        // 这里需要实现令牌生成逻辑
        return null;
    }

    @Override
    public boolean validatePassword(String rawPassword, String encodedPassword) {
        // 这里需要实现密码验证逻辑
        return false;
    }

    @Override
    public User userid2basic(Long userid, List<String> addField) {
        // 这里需要实现用户基本信息获取逻辑
        return null;
    }

    @Override
    public List<User> searchUser(String keyword, Integer limit) {
        // 这里需要实现用户搜索逻辑
        return new ArrayList<>();
    }

    @Override
    public void changeUserPassword(Long userId, String newPassword) {
        // 这里需要实现修改密码逻辑
    }

    @Override
    public void changeUserStatus(Long userId, String status) {
        // 这里需要实现修改用户状态逻辑
    }

    @Override
    public Map<String, Object> getUserStatistics() {
        // 这里需要实现用户统计逻辑
        return new java.util.HashMap<>();
    }

    @Override
    public List<User> getUsersByRole(String role) {
        // 这里需要实现按角色获取用户逻辑
        return new ArrayList<>();
    }

    @Override
    public void assignRole(Long userId, String role) {
        // 这里需要实现分配角色逻辑
    }

    @Override
    public void removeRole(Long userId, String role) {
        // 这里需要实现移除角色逻辑
    }

    @Override
    public List<User> getUsersByDepartment(Long departmentId) {
        // 这里需要实现按部门获取用户逻辑
        return new ArrayList<>();
    }

    @Override
    public void assignDepartment(Long userId, Long departmentId) {
        // 这里需要实现分配部门逻辑
    }

    @Override
    public void resetUserPassword(Long userId, String defaultPassword) {
        // 这里需要实现重置密码逻辑
    }

    @Override
    public List<User> getActiveUsers() {
        // 这里需要实现获取活跃用户逻辑
        return new ArrayList<>();
    }

    @Override
    public Map<String, Long> getUserCountByDepartment() {
        // 这里需要实现按部门统计用户数量逻辑
        return new java.util.HashMap<>();
    }

    /**
     * 清除用户相关缓存
     */
    private void clearUserRelatedCache(Long userId) {
        permissionCache.invalidate("user:" + userId);
        permissionCache.invalidate("admin:" + userId);

        // 清除列表缓存
        // 这里可以根据需要清除更多相关缓存
    }

    /**
     * 获取缓存统计信息
     */
    public String getCacheStats() {
        return String.format(
            "权限缓存统计: %s, 会话缓存统计: %s",
            permissionCache.stats(),
            sessionCache.stats()
        );
    }
}