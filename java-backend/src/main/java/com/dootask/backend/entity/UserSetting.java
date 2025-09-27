package com.dootask.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("user_settings")
public class UserSetting {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String settingKey;

    private String settingValue;

    private String settingType; // string, number, boolean, json

    private String category; // theme, notification, language, privacy

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}