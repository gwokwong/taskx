package com.dootask.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchResultDto {

    private String type; // project, task, file, user

    private Long id;

    private String title;

    private String description;

    private String url;

    private String icon;

    private LocalDateTime updatedAt;

    private String matchField; // 匹配的字段

    private String highlight; // 高亮内容

    private Double relevanceScore; // 相关性评分
}