package com.dootask.backend.service.impl;

import com.dootask.backend.entity.User;
import com.dootask.backend.mapper.UserMapper;
import com.dootask.backend.service.UserService;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户服务实现类 - 性能优化版本
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PerformanceOptimizedUserService implements UserService {

    private final UserMapper userMapper;
    private final Cache<String, Object> permissionCache;
    private final Cache<String, Object> sessionCache;

    @Override
    @Cacheable(value = "users", key = "#id")
    public User getUserById(Long id) {
        log.debug("从数据库获取用户信息: {}", id);
        return userMapper.selectById(id);
    }

    @Override
    @Cacheable(value = "users", key = "'email:' + #email")
    public User getUserByEmail(String email) {
        log.debug("从数据库获取用户信息: {}", email);
        return userMapper.selectOne(
            userMapper.lambdaQuery().eq(User::getEmail, email)
        );
    }

    @Override
    @Cacheable(value = "users", key = "'list:' + #page + ':' + #size")
    public List<User> getAllUsers(Integer page, Integer size) {
        log.debug("从数据库获取用户列表: page={}, size={}", page, size);

        if (page != null && size != null) {
            // 使用分页查询
            return userMapper.selectPage(
                new Page<>(page, size),
                userMapper.lambdaQuery().orderByDesc(User::getCreatedAt)
            ).getRecords();
        }

        return userMapper.selectList(
            userMapper.lambdaQuery().orderByDesc(User::getCreatedAt)
        );
    }

    @Override
    @CacheEvict(value = "users", allEntries = true)
    public User createUser(User user) {
        log.info("创建用户: {}", user.getEmail());

        // 设置默认值
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userMapper.insert(user);

        // 清除相关缓存
        clearUserRelatedCache(user.getUserid());

        return user;
    }

    @Override
    @CacheEvict(value = "users", key = "#user.userid")
    public User updateUser(User user) {
        log.info("更新用户: {}", user.getUserid());

        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);

        // 清除相关缓存
        clearUserRelatedCache(user.getUserid());

        return user;
    }

    @Override
    @CacheEvict(value = "users", key = "#id")
    public void deleteUser(Long id) {
        log.info("删除用户: {}", id);

        userMapper.deleteById(id);

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
            List<User> dbUsers = userMapper.selectBatchIds(uncachedIds);
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
            List<User> activeUsers = userMapper.selectList(
                userMapper.lambdaQuery()
                    .isNull(User::getDisableAt)
                    .ge(User::getLastAt, LocalDateTime.now().minusDays(7))
                    .orderByDesc(User::getLastAt)
                    .last("LIMIT 100")
            );

            for (User user : activeUsers) {
                permissionCache.put("user:" + user.getUserid(), user);
                permissionCache.put("admin:" + user.getUserid(), "admin".equals(user.getRole()));
            }

            log.info("用户缓存预热完成，预加载 {} 个活跃用户", activeUsers.size());

        } catch (Exception e) {
            log.error("用户缓存预热失败", e);
        }
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