package com.dootask.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dootask.backend.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT d.name as department_name, COUNT(u.userid) as count " +
            "FROM users u " +
            "LEFT JOIN departments d ON u.department_id = d.id " +
            "GROUP BY u.department_id, d.name")
    List<Map<String, Object>> getUserCountByDepartment();

    @Select("SELECT DATE(created_at) as date, COUNT(*) as count " +
            "FROM users " +
            "WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY) " +
            "GROUP BY DATE(created_at) " +
            "ORDER BY date")
    List<Map<String, Object>> getUserRegistrationStats(int days);
}