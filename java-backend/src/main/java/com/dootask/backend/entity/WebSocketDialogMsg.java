package com.dootask.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * WebSocket对话消息实体
 */
@Data
@Entity
@Table(name = "websocket_dialog_msgs")
@TableName("websocket_dialog_msgs")
public class WebSocketDialogMsg {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 对话ID
     */
    @Column(name = "dialog_id", nullable = false)
    private Long dialogId;

    /**
     * 发送用户ID
     */
    @Column(name = "userid", nullable = false)
    private Long userid;

    /**
     * 消息类型: text, file, image, audio, video, system, notice
     */
    @Column(name = "type", length = 20)
    private String type = "text";

    /**
     * 消息内容
     */
    @Column(name = "msg", columnDefinition = "TEXT")
    private String msg;

    /**
     * 文件信息(JSON格式)
     */
    @Column(name = "file", columnDefinition = "JSON")
    private String file;

    /**
     * 是否已撤回
     */
    @Column(name = "withdraw", columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean withdraw = false;

    /**
     * 撤回时间
     */
    @Column(name = "withdraw_at")
    private LocalDateTime withdrawAt;

    /**
     * 引用的消息ID
     */
    @Column(name = "quote_id")
    private Long quoteId;

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

    /**
     * 逻辑删除
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted = 0;
}