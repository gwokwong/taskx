package com.dootask.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("departments")
public class Department {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String name;

    private String description;

    private Long parentId;

    private String level; // 部门层级路径

    private Integer sort;

    private Long managerId; // 部门经理ID

    private String status; // active, inactive

    private String code; // 部门编码

    private String phone;

    private String email;

    private String address;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}