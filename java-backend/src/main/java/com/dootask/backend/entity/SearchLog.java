package com.dootask.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("search_logs")
public class SearchLog {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String keyword;

    private String searchType; // all, project, task, file, user

    private Integer resultCount;

    private LocalDateTime searchAt;
}