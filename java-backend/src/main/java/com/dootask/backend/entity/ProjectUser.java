package com.dootask.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 项目用户关联实体 - 用于兼容原系统的project_users表
 */
@Data
@Entity
@Table(name = "project_users")
@TableName("project_users")
public class ProjectUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 项目ID
     */
    @Column(name = "project_id", nullable = false)
    private Long projectId;

    /**
     * 用户ID
     */
    @Column(name = "userid", nullable = false)
    private Long userid;

    /**
     * 用户类型: owner(所有者), admin(管理员), member(成员), observer(观察者)
     */
    @Column(name = "type", length = 20)
    private String type = "member";

    /**
     * 是否接受邀请
     */
    @Column(name = "is_accept")
    private Boolean isAccept = false;

    /**
     * 邀请者ID
     */
    @Column(name = "inviter_userid")
    private Long inviterUserid;

    /**
     * 邀请时间
     */
    @Column(name = "inviter_at")
    private LocalDateTime inviterAt;

    /**
     * 接受时间
     */
    @Column(name = "accept_at")
    private LocalDateTime acceptAt;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted = 0;
}