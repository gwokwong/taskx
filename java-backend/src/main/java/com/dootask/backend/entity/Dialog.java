package com.dootask.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("dialogs")
public class Dialog {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String name;

    private String type; // user, group

    private Long createdBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}