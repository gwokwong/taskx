package com.dootask.backend.controller;

import com.dootask.backend.common.Result;
import com.dootask.backend.entity.TaskComment;
import com.dootask.backend.service.TaskCommentService;
import com.dootask.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 任务评论控制器
 */
@Slf4j
@RestController
@RequestMapping("/tasks/{taskId}/comments")
@RequiredArgsConstructor
@Tag(name = "任务评论管理", description = "任务评论相关接口")
public class TaskCommentController {

    private final TaskCommentService taskCommentService;
    private final UserService userService;

    @Operation(summary = "获取任务评论列表")
    @GetMapping
    public Result<List<TaskComment>> getComments(@PathVariable Long taskId) {
        List<TaskComment> comments = taskCommentService.getCommentsByTaskId(taskId);
        return Result.success(comments);
    }

    @Operation(summary = "添加评论")
    @PostMapping
    public Result<TaskComment> addComment(
            @PathVariable Long taskId,
            @RequestBody Map<String, Object> request,
            HttpServletRequest httpRequest) {

        // 获取当前用户ID
        String token = httpRequest.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        var user = userService.getUserByToken(token);
        if (user == null) {
            return Result.error("用户未登录");
        }

        String content = (String) request.get("content");
        Long parentId = request.get("parentId") != null ?
            Long.valueOf(request.get("parentId").toString()) : null;
        @SuppressWarnings("unchecked")
        List<Long> mentions = (List<Long>) request.get("mentions");

        if (content == null || content.trim().isEmpty()) {
            return Result.error("评论内容不能为空");
        }

        TaskComment comment = taskCommentService.addComment(
            taskId, content.trim(), user.getUserid(), parentId, mentions);

        return Result.success(comment);
    }

    @Operation(summary = "更新评论")
    @PutMapping("/{commentId}")
    public Result<TaskComment> updateComment(
            @PathVariable Long taskId,
            @PathVariable Long commentId,
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {

        // 获取当前用户ID
        String token = httpRequest.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        var user = userService.getUserByToken(token);
        if (user == null) {
            return Result.error("用户未登录");
        }

        String content = request.get("content");
        if (content == null || content.trim().isEmpty()) {
            return Result.error("评论内容不能为空");
        }

        TaskComment comment = taskCommentService.updateComment(
            commentId, content.trim(), user.getUserid());

        return Result.success(comment);
    }

    @Operation(summary = "删除评论")
    @DeleteMapping("/{commentId}")
    public Result<Void> deleteComment(
            @PathVariable Long taskId,
            @PathVariable Long commentId,
            HttpServletRequest httpRequest) {

        // 获取当前用户ID
        String token = httpRequest.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        var user = userService.getUserByToken(token);
        if (user == null) {
            return Result.error("用户未登录");
        }

        taskCommentService.deleteComment(commentId, user.getUserid());
        return Result.success();
    }

    @Operation(summary = "获取评论回复")
    @GetMapping("/{commentId}/replies")
    public Result<List<TaskComment>> getReplies(
            @PathVariable Long taskId,
            @PathVariable Long commentId) {

        List<TaskComment> replies = taskCommentService.getRepliesByParentId(commentId);
        return Result.success(replies);
    }

    @Operation(summary = "获取任务评论数量")
    @GetMapping("/count")
    public Result<Integer> getCommentCount(@PathVariable Long taskId) {
        Integer count = taskCommentService.getCommentCountByTaskId(taskId);
        return Result.success(count);
    }
}