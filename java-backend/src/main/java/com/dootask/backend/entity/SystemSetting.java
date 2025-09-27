package com.dootask.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("system_settings")
public class SystemSetting {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String settingKey;

    private String settingValue;

    private String settingType; // string, number, boolean, json

    private String category; // general, email, storage, security

    private String description;

    private Boolean isPublic; // 是否为公开设置

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}