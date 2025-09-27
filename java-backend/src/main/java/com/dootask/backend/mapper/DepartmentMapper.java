package com.dootask.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dootask.backend.entity.Department;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface DepartmentMapper extends BaseMapper<Department> {

    @Select("SELECT COUNT(*) FROM users WHERE department_id = #{departmentId}")
    Long getDepartmentUserCount(Long departmentId);

    @Select("SELECT d.id, d.name, d.code, COUNT(u.userid) as user_count " +
            "FROM departments d " +
            "LEFT JOIN users u ON d.id = u.department_id " +
            "GROUP BY d.id, d.name, d.code " +
            "ORDER BY d.sort, d.id")
    List<Map<String, Object>> getDepartmentUserCountList();

    @Select("SELECT d.*, u.nickname as manager_name " +
            "FROM departments d " +
            "LEFT JOIN users u ON d.manager_id = u.userid " +
            "WHERE d.status = 'active' " +
            "ORDER BY d.sort, d.id")
    List<Map<String, Object>> getDepartmentWithManager();
}