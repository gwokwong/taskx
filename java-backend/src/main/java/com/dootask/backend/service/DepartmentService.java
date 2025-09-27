package com.dootask.backend.service;

import com.dootask.backend.entity.Department;

import java.util.List;
import java.util.Map;

public interface DepartmentService {

    Department create(Department department);

    Department update(Department department);

    void delete(Long id);

    Department getById(Long id);

    List<Department> getAllDepartments();

    List<Department> getDepartmentTree();

    List<Department> getChildDepartments(Long parentId);

    List<Department> searchDepartments(String keyword);

    void moveDepartment(Long id, Long newParentId);

    Map<String, Object> getDepartmentStatistics();

    List<Department> getDepartmentsByManager(Long managerId);

    void assignManager(Long departmentId, Long managerId);

    boolean hasChildDepartments(Long id);

    List<Map<String, Object>> getDepartmentUserCount();

    Department getDepartmentByCode(String code);
}