package com.dootask.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "websocket_dialogs")
public class WebSocketDialog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "type")
    private String type; // group, user

    @Column(name = "group_type")
    private String groupType; // project, task, all

    @Column(name = "group_id")
    private Long groupId;

    @Column(name = "last_at")
    private LocalDateTime lastAt;

    @Column(name = "last_msg_id")
    private Long lastMsgId;

    @Column(name = "last_msg")
    private String lastMsg;

    @TableField(fill = FieldFill.INSERT)
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted = 0;
}