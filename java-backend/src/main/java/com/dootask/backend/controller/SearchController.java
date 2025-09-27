package com.dootask.backend.controller;

import com.dootask.backend.common.Result;
import com.dootask.backend.dto.SearchResultDto;
import com.dootask.backend.service.SearchService;
import com.dootask.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@Tag(name = "搜索管理", description = "全局搜索相关接口")
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;
    private final UserService userService;

    @Operation(summary = "全局搜索")
    @GetMapping
    public Result<List<SearchResultDto>> globalSearch(
            @RequestParam String keyword,
            @RequestParam(required = false) String type,
            HttpServletRequest request) {

        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        if (keyword == null || keyword.trim().isEmpty()) {
            return Result.error("搜索关键词不能为空");
        }

        List<SearchResultDto> results = searchService.globalSearch(keyword.trim(), userId, type);
        return Result.success(results);
    }

    @Operation(summary = "搜索项目")
    @GetMapping("/projects")
    public Result<List<SearchResultDto>> searchProjects(
            @RequestParam String keyword,
            HttpServletRequest request) {

        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        List<SearchResultDto> results = searchService.searchProjects(keyword.trim(), userId);
        return Result.success(results);
    }

    @Operation(summary = "搜索任务")
    @GetMapping("/tasks")
    public Result<List<SearchResultDto>> searchTasks(
            @RequestParam String keyword,
            HttpServletRequest request) {

        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        List<SearchResultDto> results = searchService.searchTasks(keyword.trim(), userId);
        return Result.success(results);
    }

    @Operation(summary = "搜索文件")
    @GetMapping("/files")
    public Result<List<SearchResultDto>> searchFiles(
            @RequestParam String keyword,
            HttpServletRequest request) {

        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        List<SearchResultDto> results = searchService.searchFiles(keyword.trim(), userId);
        return Result.success(results);
    }

    @Operation(summary = "搜索用户")
    @GetMapping("/users")
    public Result<List<SearchResultDto>> searchUsers(
            @RequestParam String keyword,
            HttpServletRequest request) {

        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        List<SearchResultDto> results = searchService.searchUsers(keyword.trim(), userId);
        return Result.success(results);
    }

    @Operation(summary = "获取搜索建议")
    @GetMapping("/suggestions")
    public Result<List<String>> getSearchSuggestions(
            @RequestParam String keyword,
            HttpServletRequest request) {

        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        List<String> suggestions = searchService.getSearchSuggestions(keyword.trim(), userId);
        return Result.success(suggestions);
    }

    @Operation(summary = "获取最近搜索")
    @GetMapping("/recent")
    public Result<List<String>> getRecentSearches(HttpServletRequest request) {

        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }

        List<String> recentSearches = searchService.getRecentSearches(userId);
        return Result.success(recentSearches);
    }

    private Long getCurrentUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            var user = userService.getUserByToken(token);
            return user != null ? user.getUserid() : null;
        }
        return null;
    }
}