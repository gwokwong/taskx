package com.dootask.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dootask.backend.entity.ProjectMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface ProjectMemberMapper extends BaseMapper<ProjectMember> {

    @Select("SELECT p.name as project_name, COUNT(pm.user_id) as member_count " +
            "FROM projects p " +
            "LEFT JOIN project_members pm ON p.id = pm.project_id AND pm.status = 'active' " +
            "GROUP BY p.id, p.name " +
            "ORDER BY member_count DESC")
    List<Map<String, Object>> getProjectMemberStats();

    @Select("SELECT pm.*, u.nickname, u.email, u.avatar " +
            "FROM project_members pm " +
            "LEFT JOIN users u ON pm.user_id = u.userid " +
            "WHERE pm.project_id = #{projectId} " +
            "AND pm.status = 'active' " +
            "AND (u.nickname LIKE CONCAT('%', #{keyword}, '%') OR u.email LIKE CONCAT('%', #{keyword}, '%')) " +
            "ORDER BY pm.role, pm.joined_at")
    List<ProjectMember> searchProjectMembers(Long projectId, String keyword);

    @Select("SELECT pm.*, u.nickname, u.email, u.avatar, p.name as project_name " +
            "FROM project_members pm " +
            "LEFT JOIN users u ON pm.user_id = u.userid " +
            "LEFT JOIN projects p ON pm.project_id = p.id " +
            "WHERE pm.user_id = #{userId} AND pm.status = 'active' " +
            "ORDER BY pm.joined_at DESC")
    List<Map<String, Object>> getUserProjectsWithDetails(Long userId);
}