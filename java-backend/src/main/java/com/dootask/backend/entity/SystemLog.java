package com.dootask.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("system_logs")
public class SystemLog {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String userName;

    private String action; // LOGIN, LOGOUT, CREATE, UPDATE, DELETE, UPLOAD, DOWNLOAD

    private String module; // USER, PROJECT, TASK, FILE, MESSAGE

    private String description;

    private String ipAddress;

    private String userAgent;

    private String requestPath;

    private String requestMethod;

    private String requestParams;

    private String responseStatus;

    private Long executionTime; // 执行时间(毫秒)

    private LocalDateTime createdAt;
}