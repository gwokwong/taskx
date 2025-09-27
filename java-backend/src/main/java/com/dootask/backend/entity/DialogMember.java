package com.dootask.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("dialog_members")
public class DialogMember {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long dialogId;

    private Long userId;

    private String role; // admin, member

    private LocalDateTime joinedAt;

    private LocalDateTime lastReadAt;
}