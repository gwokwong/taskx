package com.dootask.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("messages")
public class Message {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long dialogId;

    private Long userId;

    private String content;

    private String messageType;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}