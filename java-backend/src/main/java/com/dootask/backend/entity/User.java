package com.dootask.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(value = "userid", type = IdType.AUTO)
    private Long userid;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String nickname;

    private String password;

    private String encrypt;

    @Column(name = "user_img")
    private String userImg;

    private String profession;

    private String tel;

    @Column(columnDefinition = "JSON")
    private String identity;

    @Column(columnDefinition = "JSON")
    private String department;

    private String az;

    private String pinyin;

    @Column(name = "login_num")
    private Integer loginNum = 0;

    @Column(name = "last_ip")
    private String lastIp;

    @Column(name = "last_at")
    private LocalDateTime lastAt;

    @Column(name = "line_ip")
    private String lineIp;

    @Column(name = "line_at")
    private LocalDateTime lineAt;

    @Column(name = "task_dialog_id")
    private Long taskDialogId;

    @Column(name = "created_ip")
    private String createdIp;

    @Column(name = "disable_at")
    private LocalDateTime disableAt;

    @Column(name = "email_verity")
    private Integer emailVerity = 0;

    @Column(name = "changepass")
    private Integer changepass = 0;

    private Integer bot = 0;

    private String lang = "zh-CN";

    @TableField(fill = FieldFill.INSERT)
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted = 0;

    @Transient
    @TableField(exist = false)
    private Boolean online;

    @Transient
    @TableField(exist = false)
    private String departmentName;

    @Transient
    @TableField(exist = false)
    private List<String> identityList;

    @Transient
    @TableField(exist = false)
    private List<Long> departmentList;
}