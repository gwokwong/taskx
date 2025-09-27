package com.dootask.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("calendar_events")
public class CalendarEvent {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String title;

    private String description;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Boolean allDay;

    private String color;

    private String location;

    private String type; // task, meeting, reminder, birthday

    private Long relatedId; // 关联的任务或项目ID

    private String relatedType; // task, project

    private String recurrenceRule; // 重复规则

    private String reminder; // 提醒时间

    private String status; // pending, confirmed, cancelled

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}