package com.dootask.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "websocket_dialog_messages")
public class WebSocketDialogMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Column(name = "dialog_id")
    private Long dialogId;

    @Column(name = "userid")
    private Long userid;

    @Column(name = "type")
    private String type; // text, image, file, system

    @Column(columnDefinition = "TEXT")
    private String msg;

    @Column(name = "send_at")
    private LocalDateTime sendAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "reply_id")
    private Long replyId;

    @Column(name = "todo_status")
    private String todoStatus;

    @Column(name = "mention")
    private String mention; // JSON array of user IDs

    @Column(name = "files")
    private String files; // JSON array of file info

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