package com.dootask.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dootask.backend.dto.SearchResultDto;
import com.dootask.backend.entity.*;
import com.dootask.backend.mapper.*;
import com.dootask.backend.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final ProjectMapper projectMapper;
    private final ProjectTaskMapper projectTaskMapper;
    private final FileMapper fileMapper;
    private final UserMapper userMapper;
    private final SearchLogMapper searchLogMapper;

    @Override
    public List<SearchResultDto> globalSearch(String keyword, Long userId, String type) {
        List<SearchResultDto> results = new ArrayList<>();

        if ("all".equals(type) || type == null) {
            results.addAll(searchProjects(keyword, userId));
            results.addAll(searchTasks(keyword, userId));
            results.addAll(searchFiles(keyword, userId));
            results.addAll(searchUsers(keyword, userId));
        } else {
            switch (type) {
                case "project":
                    results.addAll(searchProjects(keyword, userId));
                    break;
                case "task":
                    results.addAll(searchTasks(keyword, userId));
                    break;
                case "file":
                    results.addAll(searchFiles(keyword, userId));
                    break;
                case "user":
                    results.addAll(searchUsers(keyword, userId));
                    break;
            }
        }

        // 按相关性评分排序
        results.sort(Comparator.comparing(SearchResultDto::getRelevanceScore).reversed());

        // 记录搜索日志
        logSearch(userId, keyword, type, results.size());

        return results.stream().limit(50).collect(Collectors.toList());
    }

    @Override
    public List<SearchResultDto> searchProjects(String keyword, Long userId) {
        List<Project> projects = projectMapper.selectList(new QueryWrapper<Project>()
                .and(wrapper -> wrapper
                        .like("name", keyword)
                        .or()
                        .like("desc", keyword))
                .orderByDesc("updated_at"));

        return projects.stream().map(project -> {
            SearchResultDto result = new SearchResultDto();
            result.setType("project");
            result.setId(project.getId());
            result.setTitle(project.getName());
            result.setDescription(project.getDesc());
            result.setUrl("/projects/" + project.getId());
            result.setIcon("folder");
            result.setUpdatedAt(project.getUpdatedAt());
            result.setMatchField(project.getName().toLowerCase().contains(keyword.toLowerCase()) ? "name" : "desc");
            result.setHighlight(highlightKeyword(
                    project.getName().toLowerCase().contains(keyword.toLowerCase()) ? project.getName() : project.getDesc(),
                    keyword
            ));
            result.setRelevanceScore(calculateRelevanceScore(keyword, project.getName(), project.getDesc()));
            return result;
        }).collect(Collectors.toList());
    }

    @Override
    public List<SearchResultDto> searchTasks(String keyword, Long userId) {
        List<ProjectTask> tasks = projectTaskMapper.selectList(new QueryWrapper<ProjectTask>()
                .and(wrapper -> wrapper
                        .like("name", keyword)
                        .or()
                        .like("desc", keyword))
                .orderByDesc("updated_at"));

        return tasks.stream().map(task -> {
            SearchResultDto result = new SearchResultDto();
            result.setType("task");
            result.setId(task.getId());
            result.setTitle(task.getName());
            result.setDescription(task.getDesc());
            result.setUrl("/tasks/" + task.getId());
            result.setIcon("check-circle");
            result.setUpdatedAt(task.getUpdatedAt());
            result.setMatchField(task.getName().toLowerCase().contains(keyword.toLowerCase()) ? "name" : "desc");
            result.setHighlight(highlightKeyword(
                    task.getName().toLowerCase().contains(keyword.toLowerCase()) ? task.getName() : task.getDesc(),
                    keyword
            ));
            result.setRelevanceScore(calculateRelevanceScore(keyword, task.getName(), task.getDesc()));
            return result;
        }).collect(Collectors.toList());
    }

    @Override
    public List<SearchResultDto> searchFiles(String keyword, Long userId) {
        List<FileEntity> files = fileMapper.selectList(new QueryWrapper<FileEntity>()
                .like("file_name", keyword)
                .orderByDesc("updated_at"));

        return files.stream().map(file -> {
            SearchResultDto result = new SearchResultDto();
            result.setType("file");
            result.setId(file.getId());
            result.setTitle(file.getFileName());
            result.setDescription("文件大小: " + formatFileSize(file.getFileSize()));
            result.setUrl("/files/" + file.getId());
            result.setIcon("document");
            result.setUpdatedAt(file.getUpdatedAt());
            result.setMatchField("name");
            result.setHighlight(highlightKeyword(file.getFileName(), keyword));
            result.setRelevanceScore(calculateRelevanceScore(keyword, file.getFileName(), ""));
            return result;
        }).collect(Collectors.toList());
    }

    @Override
    public List<SearchResultDto> searchUsers(String keyword, Long userId) {
        List<User> users = userMapper.selectList(new QueryWrapper<User>()
                .and(wrapper -> wrapper
                        .like("nickname", keyword)
                        .or()
                        .like("email", keyword))
                .orderByDesc("created_at"));

        return users.stream().map(user -> {
            SearchResultDto result = new SearchResultDto();
            result.setType("user");
            result.setId(user.getUserid());
            result.setTitle(user.getNickname());
            result.setDescription(user.getEmail());
            result.setUrl("/users/" + user.getUserid());
            result.setIcon("user");
            result.setUpdatedAt(user.getCreatedAt());
            result.setMatchField(user.getNickname().toLowerCase().contains(keyword.toLowerCase()) ? "name" : "email");
            result.setHighlight(highlightKeyword(
                    user.getNickname().toLowerCase().contains(keyword.toLowerCase()) ? user.getNickname() : user.getEmail(),
                    keyword
            ));
            result.setRelevanceScore(calculateRelevanceScore(keyword, user.getNickname(), user.getEmail()));
            return result;
        }).collect(Collectors.toList());
    }

    @Override
    public List<String> getSearchSuggestions(String keyword, Long userId) {
        return searchLogMapper.getSearchSuggestions(keyword);
    }

    @Override
    public List<String> getRecentSearches(Long userId) {
        return searchLogMapper.getRecentSearches(userId);
    }

    @Override
    public void logSearch(Long userId, String keyword, String type, Integer resultCount) {
        SearchLog log = new SearchLog();
        log.setUserId(userId);
        log.setKeyword(keyword);
        log.setSearchType(type != null ? type : "all");
        log.setResultCount(resultCount);
        log.setSearchAt(LocalDateTime.now());
        searchLogMapper.insert(log);
    }

    private Double calculateRelevanceScore(String keyword, String title, String description) {
        double score = 0.0;
        String lowerKeyword = keyword.toLowerCase();
        String lowerTitle = title != null ? title.toLowerCase() : "";
        String lowerDescription = description != null ? description.toLowerCase() : "";

        // 标题完全匹配
        if (lowerTitle.equals(lowerKeyword)) {
            score += 100.0;
        }
        // 标题开头匹配
        else if (lowerTitle.startsWith(lowerKeyword)) {
            score += 80.0;
        }
        // 标题包含
        else if (lowerTitle.contains(lowerKeyword)) {
            score += 60.0;
        }

        // 描述匹配
        if (lowerDescription.contains(lowerKeyword)) {
            score += 30.0;
        }

        // 关键词长度权重
        score += keyword.length() * 2;

        return score;
    }

    private String highlightKeyword(String text, String keyword) {
        if (text == null || keyword == null) return text;

        // 简单的高亮处理，在实际应用中可以使用更复杂的算法
        return text.replaceAll("(?i)" + keyword, "<mark>" + keyword + "</mark>");
    }

    private String formatFileSize(Long size) {
        if (size == null) return "0 B";

        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        double fileSize = size.doubleValue();

        while (fileSize >= 1024 && unitIndex < units.length - 1) {
            fileSize /= 1024;
            unitIndex++;
        }

        return String.format("%.1f %s", fileSize, units[unitIndex]);
    }
}