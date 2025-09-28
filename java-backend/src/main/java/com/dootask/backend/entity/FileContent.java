package com.dootask.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文件内容实体
 */
@Data
@Entity
@Table(name = "file_contents")
@TableName("file_contents")
public class FileContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 文件ID
     */
    @Column(name = "fid", nullable = false)
    private Long fid;

    /**
     * 内容
     */
    @Column(name = "content", columnDefinition = "LONGTEXT")
    private String content;

    /**
     * 文件内容类型
     */
    @Column(name = "text")
    private String text;

    /**
     * 内容大小(字节)
     */
    @Column(name = "size")
    private Long size;

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
}