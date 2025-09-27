package com.dootask.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dootask.backend.common.exception.ApiException;
import com.dootask.backend.entity.ProjectMember;
import com.dootask.backend.mapper.ProjectMemberMapper;
import com.dootask.backend.service.ProjectMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProjectMemberServiceImpl extends ServiceImpl<ProjectMemberMapper, ProjectMember> implements ProjectMemberService {

    @Override
    public ProjectMember addMember(Long projectId, Long userId, String role, Long invitedBy) {
        if (projectId == null || userId == null) {
            throw new ApiException("项目ID和用户ID不能为空");
        }

        // 检查是否已经是成员
        if (isMember(projectId, userId)) {
            throw new ApiException("用户已经是项目成员");
        }

        ProjectMember member = new ProjectMember();
        member.setProjectId(projectId);
        member.setUserId(userId);
        member.setRole(role != null ? role : "member");
        member.setStatus("active");
        member.setInvitedAt(LocalDateTime.now());
        member.setInvitedBy(invitedBy);
        member.setJoinedAt(LocalDateTime.now());
        member.setCreatedAt(LocalDateTime.now());
        member.setUpdatedAt(LocalDateTime.now());

        save(member);
        return member;
    }

    @Override
    public void removeMember(Long projectId, Long userId) {
        if (projectId == null || userId == null) {
            throw new ApiException("项目ID和用户ID不能为空");
        }

        ProjectMember member = getMember(projectId, userId);
        if (member == null) {
            throw new ApiException("用户不是项目成员");
        }

        if ("owner".equals(member.getRole())) {
            throw new ApiException("不能移除项目所有者");
        }

        member.setStatus("inactive");
        member.setLeftAt(LocalDateTime.now());
        member.setUpdatedAt(LocalDateTime.now());
        updateById(member);
    }

    @Override
    public void updateMemberRole(Long projectId, Long userId, String role) {
        if (projectId == null || userId == null) {
            throw new ApiException("项目ID和用户ID不能为空");
        }

        ProjectMember member = getMember(projectId, userId);
        if (member == null) {
            throw new ApiException("用户不是项目成员");
        }

        member.setRole(role);
        member.setUpdatedAt(LocalDateTime.now());
        updateById(member);
    }

    @Override
    public void updateMemberPermissions(Long projectId, Long userId, String permissions) {
        if (projectId == null || userId == null) {
            throw new ApiException("项目ID和用户ID不能为空");
        }

        ProjectMember member = getMember(projectId, userId);
        if (member == null) {
            throw new ApiException("用户不是项目成员");
        }

        member.setPermissions(permissions);
        member.setUpdatedAt(LocalDateTime.now());
        updateById(member);
    }

    @Override
    public List<ProjectMember> getProjectMembers(Long projectId) {
        if (projectId == null) {
            return List.of();
        }

        QueryWrapper<ProjectMember> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("project_id", projectId)
                .eq("status", "active")
                .orderByAsc("role", "joined_at");
        return list(queryWrapper);
    }

    @Override
    public List<ProjectMember> getUserProjects(Long userId) {
        if (userId == null) {
            return List.of();
        }

        QueryWrapper<ProjectMember> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .eq("status", "active")
                .orderByDesc("joined_at");
        return list(queryWrapper);
    }

    @Override
    public ProjectMember getMember(Long projectId, Long userId) {
        if (projectId == null || userId == null) {
            return null;
        }

        QueryWrapper<ProjectMember> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("project_id", projectId)
                .eq("user_id", userId)
                .eq("status", "active");
        return getOne(queryWrapper);
    }

    @Override
    public boolean isMember(Long projectId, Long userId) {
        return getMember(projectId, userId) != null;
    }

    @Override
    public boolean hasPermission(Long projectId, Long userId, String permission) {
        ProjectMember member = getMember(projectId, userId);
        if (member == null) {
            return false;
        }

        // 项目所有者拥有所有权限
        if ("owner".equals(member.getRole())) {
            return true;
        }

        // 简化权限检查，实际应该解析permissions JSON
        String permissions = member.getPermissions();
        return permissions != null && permissions.contains(permission);
    }

    @Override
    public List<ProjectMember> getPendingInvitations(Long userId) {
        if (userId == null) {
            return List.of();
        }

        QueryWrapper<ProjectMember> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .eq("status", "pending")
                .orderByDesc("invited_at");
        return list(queryWrapper);
    }

    @Override
    public void acceptInvitation(Long projectId, Long userId) {
        if (projectId == null || userId == null) {
            throw new ApiException("项目ID和用户ID不能为空");
        }

        QueryWrapper<ProjectMember> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("project_id", projectId)
                .eq("user_id", userId)
                .eq("status", "pending");

        ProjectMember member = getOne(queryWrapper);
        if (member == null) {
            throw new ApiException("邀请不存在");
        }

        member.setStatus("active");
        member.setJoinedAt(LocalDateTime.now());
        member.setUpdatedAt(LocalDateTime.now());
        updateById(member);
    }

    @Override
    public void rejectInvitation(Long projectId, Long userId) {
        if (projectId == null || userId == null) {
            throw new ApiException("项目ID和用户ID不能为空");
        }

        QueryWrapper<ProjectMember> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("project_id", projectId)
                .eq("user_id", userId)
                .eq("status", "pending");

        ProjectMember member = getOne(queryWrapper);
        if (member == null) {
            throw new ApiException("邀请不存在");
        }

        removeById(member.getId());
    }

    @Override
    public Map<String, Long> getMemberStatistics(Long projectId) {
        if (projectId == null) {
            return new HashMap<>();
        }

        Map<String, Long> stats = new HashMap<>();

        // 总成员数
        long totalMembers = count(new QueryWrapper<ProjectMember>()
                .eq("project_id", projectId)
                .eq("status", "active"));
        stats.put("totalMembers", totalMembers);

        // 各角色成员数
        long owners = count(new QueryWrapper<ProjectMember>()
                .eq("project_id", projectId)
                .eq("role", "owner")
                .eq("status", "active"));
        stats.put("owners", owners);

        long members = count(new QueryWrapper<ProjectMember>()
                .eq("project_id", projectId)
                .eq("role", "member")
                .eq("status", "active"));
        stats.put("members", members);

        long viewers = count(new QueryWrapper<ProjectMember>()
                .eq("project_id", projectId)
                .eq("role", "viewer")
                .eq("status", "active"));
        stats.put("viewers", viewers);

        return stats;
    }

    @Override
    public List<Map<String, Object>> getProjectMemberStats() {
        return baseMapper.getProjectMemberStats();
    }

    @Override
    public void transferOwnership(Long projectId, Long newOwnerId, Long currentOwnerId) {
        if (projectId == null || newOwnerId == null || currentOwnerId == null) {
            throw new ApiException("参数不能为空");
        }

        // 检查当前所有者
        ProjectMember currentOwner = getMember(projectId, currentOwnerId);
        if (currentOwner == null || !"owner".equals(currentOwner.getRole())) {
            throw new ApiException("当前用户不是项目所有者");
        }

        // 检查新所有者是否是成员
        ProjectMember newOwner = getMember(projectId, newOwnerId);
        if (newOwner == null) {
            throw new ApiException("新所有者不是项目成员");
        }

        // 更新角色
        currentOwner.setRole("member");
        currentOwner.setUpdatedAt(LocalDateTime.now());
        updateById(currentOwner);

        newOwner.setRole("owner");
        newOwner.setUpdatedAt(LocalDateTime.now());
        updateById(newOwner);
    }

    @Override
    public List<ProjectMember> searchMembers(Long projectId, String keyword) {
        if (projectId == null) {
            return List.of();
        }

        return baseMapper.searchProjectMembers(projectId, keyword);
    }

    @Override
    public void bulkAddMembers(Long projectId, List<Long> userIds, String role, Long invitedBy) {
        if (projectId == null || userIds == null || userIds.isEmpty()) {
            throw new ApiException("参数不能为空");
        }

        for (Long userId : userIds) {
            try {
                if (!isMember(projectId, userId)) {
                    addMember(projectId, userId, role, invitedBy);
                }
            } catch (Exception e) {
                // 忽略单个用户添加失败的情况
            }
        }
    }

    @Override
    public void bulkRemoveMembers(Long projectId, List<Long> userIds) {
        if (projectId == null || userIds == null || userIds.isEmpty()) {
            throw new ApiException("参数不能为空");
        }

        for (Long userId : userIds) {
            try {
                removeMember(projectId, userId);
            } catch (Exception e) {
                // 忽略单个用户移除失败的情况
            }
        }
    }
}