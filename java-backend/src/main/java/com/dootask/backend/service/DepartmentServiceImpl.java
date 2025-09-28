package com.dootask.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dootask.backend.common.exception.ApiException;
import com.dootask.backend.entity.Department;
import com.dootask.backend.mapper.DepartmentMapper;
import com.dootask.backend.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department> implements DepartmentService {

    @Override
    public Department create(Department department) {
        if (!StringUtils.hasText(department.getName())) {
            throw new ApiException("部门名称不能为空");
        }

        // 检查部门名称是否已存在
        if (count(new QueryWrapper<Department>().eq("name", department.getName())) > 0) {
            throw new ApiException("部门名称已存在");
        }

        // 检查部门编码是否已存在
        if (StringUtils.hasText(department.getCode()) &&
            count(new QueryWrapper<Department>().eq("code", department.getCode())) > 0) {
            throw new ApiException("部门编码已存在");
        }

        // 设置层级路径
        if (department.getParentId() != null && department.getParentId() > 0) {
            Department parent = getById(department.getParentId());
            if (parent == null) {
                throw new ApiException("上级部门不存在");
            }
            department.setLevel(parent.getLevel() + "/" + department.getParentId());
        } else {
            department.setParentId(0L);
            department.setLevel("0");
        }

        if (department.getStatus() == null) {
            department.setStatus("active");
        }

        department.setCreatedAt(LocalDateTime.now());
        department.setUpdatedAt(LocalDateTime.now());

        save(department);
        return department;
    }

    @Override
    public Department update(Department department) {
        if (department.getId() == null) {
            throw new ApiException("部门ID不能为空");
        }

        Department existing = getById(department.getId());
        if (existing == null) {
            throw new ApiException("部门不存在");
        }

        // 检查名称冲突
        if (StringUtils.hasText(department.getName()) && !department.getName().equals(existing.getName())) {
            if (count(new QueryWrapper<Department>().eq("name", department.getName()).ne("id", department.getId())) > 0) {
                throw new ApiException("部门名称已存在");
            }
        }

        // 检查编码冲突
        if (StringUtils.hasText(department.getCode()) && !department.getCode().equals(existing.getCode())) {
            if (count(new QueryWrapper<Department>().eq("code", department.getCode()).ne("id", department.getId())) > 0) {
                throw new ApiException("部门编码已存在");
            }
        }

        department.setUpdatedAt(LocalDateTime.now());
        updateById(department);
        return department;
    }

    @Override
    public void delete(Long id) {
        if (id == null || id <= 0) {
            throw new ApiException("部门ID不能为空");
        }

        Department department = getById(id);
        if (department == null) {
            throw new ApiException("部门不存在");
        }

        // 检查是否有子部门
        if (hasChildDepartments(id)) {
            throw new ApiException("存在子部门，无法删除");
        }

        // 检查是否有员工
        if (baseMapper.getDepartmentUserCount(id) > 0) {
            throw new ApiException("部门下还有员工，无法删除");
        }

        removeById(id);
    }

    @Override
    public Department getById(Long id) {
        if (id == null || id <= 0) {
            return null;
        }
        return super.getById(id);
    }

    @Override
    public List<Department> getAllDepartments() {
        QueryWrapper<Department> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByAsc("sort", "id");
        return list(queryWrapper);
    }

    @Override
    public List<Department> getDepartmentTree() {
        // 简化实现，返回所有部门，实际应该构建树形结构
        return getAllDepartments();
    }

    @Override
    public List<Department> getChildDepartments(Long parentId) {
        QueryWrapper<Department> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_id", parentId != null ? parentId : 0)
                .orderByAsc("sort", "id");
        return list(queryWrapper);
    }

    @Override
    public List<Department> searchDepartments(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return getAllDepartments();
        }

        QueryWrapper<Department> queryWrapper = new QueryWrapper<>();
        queryWrapper.and(wrapper -> wrapper
                .like("name", keyword)
                .or()
                .like("code", keyword)
                .or()
                .like("description", keyword))
                .orderByAsc("sort", "id");
        return list(queryWrapper);
    }

    @Override
    public void moveDepartment(Long id, Long newParentId) {
        if (id == null || id <= 0) {
            throw new ApiException("部门ID不能为空");
        }

        Department department = getById(id);
        if (department == null) {
            throw new ApiException("部门不存在");
        }

        // 检查是否移动到自己或子部门下
        if (newParentId != null && newParentId.equals(id)) {
            throw new ApiException("不能移动到自己下面");
        }

        // 更新层级路径
        String newLevel;
        if (newParentId != null && newParentId > 0) {
            Department newParent = getById(newParentId);
            if (newParent == null) {
                throw new ApiException("目标父部门不存在");
            }

            // 检查是否移动到子部门下
            if (newParent.getLevel().contains("/" + id + "/") || newParent.getLevel().endsWith("/" + id)) {
                throw new ApiException("不能移动到子部门下");
            }

            newLevel = newParent.getLevel() + "/" + newParentId;
        } else {
            newParentId = 0L;
            newLevel = "0";
        }

        department.setParentId(newParentId);
        department.setLevel(newLevel);
        department.setUpdatedAt(LocalDateTime.now());
        updateById(department);

        // TODO: 更新所有子部门的层级路径
    }

    @Override
    public Map<String, Object> getDepartmentStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // 总部门数
        long totalDepartments = count();
        stats.put("totalDepartments", totalDepartments);

        // 活跃部门数
        long activeDepartments = count(new QueryWrapper<Department>().eq("status", "active"));
        stats.put("activeDepartments", activeDepartments);

        // 一级部门数
        long topLevelDepartments = count(new QueryWrapper<Department>().eq("parent_id", 0));
        stats.put("topLevelDepartments", topLevelDepartments);

        return stats;
    }

    @Override
    public List<Department> getDepartmentsByManager(Long managerId) {
        if (managerId == null || managerId <= 0) {
            return List.of();
        }

        QueryWrapper<Department> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("manager_id", managerId)
                .orderByAsc("sort", "id");
        return list(queryWrapper);
    }

    @Override
    public void assignManager(Long departmentId, Long managerId) {
        if (departmentId == null || departmentId <= 0) {
            throw new ApiException("部门ID不能为空");
        }

        Department department = getById(departmentId);
        if (department == null) {
            throw new ApiException("部门不存在");
        }

        department.setManagerId(managerId);
        department.setUpdatedAt(LocalDateTime.now());
        updateById(department);
    }

    @Override
    public boolean hasChildDepartments(Long id) {
        if (id == null || id <= 0) {
            return false;
        }

        return count(new QueryWrapper<Department>().eq("parent_id", id)) > 0;
    }

    @Override
    public List<Map<String, Object>> getDepartmentUserCount() {
        return baseMapper.getDepartmentUserCountList();
    }

    @Override
    public Department getDepartmentByCode(String code) {
        if (!StringUtils.hasText(code)) {
            return null;
        }

        return getOne(new QueryWrapper<Department>().eq("code", code));
    }
}