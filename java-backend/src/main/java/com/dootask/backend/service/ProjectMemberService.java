package com.dootask.backend.service;

import com.dootask.backend.entity.ProjectMember;

import java.util.List;
import java.util.Map;

public interface ProjectMemberService {

    ProjectMember addMember(Long projectId, Long userId, String role, Long invitedBy);

    void removeMember(Long projectId, Long userId);

    void updateMemberRole(Long projectId, Long userId, String role);

    void updateMemberPermissions(Long projectId, Long userId, String permissions);

    List<ProjectMember> getProjectMembers(Long projectId);

    List<ProjectMember> getUserProjects(Long userId);

    ProjectMember getMember(Long projectId, Long userId);

    boolean isMember(Long projectId, Long userId);

    boolean hasPermission(Long projectId, Long userId, String permission);

    List<ProjectMember> getPendingInvitations(Long userId);

    void acceptInvitation(Long projectId, Long userId);

    void rejectInvitation(Long projectId, Long userId);

    Map<String, Long> getMemberStatistics(Long projectId);

    List<Map<String, Object>> getProjectMemberStats();

    void transferOwnership(Long projectId, Long newOwnerId, Long currentOwnerId);

    List<ProjectMember> searchMembers(Long projectId, String keyword);

    void bulkAddMembers(Long projectId, List<Long> userIds, String role, Long invitedBy);

    void bulkRemoveMembers(Long projectId, List<Long> userIds);
}