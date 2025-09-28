package com.dootask.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统设置实体
 */
@Data
@Entity
@Table(name = "settings")
@TableName("settings")
public class Setting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 设置键名
     */
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    /**
     * 设置描述
     */
    @Column(name = "desc")
    private String desc;

    /**
     * 设置值
     */
    @Column(name = "value", columnDefinition = "TEXT")
    private String value;

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
}