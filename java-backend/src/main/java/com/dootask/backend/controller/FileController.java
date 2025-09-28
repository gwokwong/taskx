package com.dootask.backend.controller;

import com.dootask.backend.common.Result;
import com.dootask.backend.entity.FileEntity;
import com.dootask.backend.entity.User;
import com.dootask.backend.service.FileService;
import com.dootask.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * 文件管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Tag(name = "文件管理", description = "文件上传、下载、管理相关接口")
public class FileController {

    private final FileService fileService;
    private final UserService userService;

    @Operation(summary = "上传文件")
    @PostMapping("/upload")
    public Result<FileEntity> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "projectId", required = false) Long projectId,
            @RequestParam(value = "taskId", required = false) Long taskId,
            @RequestParam(value = "folderId", required = false) Long folderId,
            HttpServletRequest request) {

        User user = getCurrentUser(request);
        if (user == null) {
            return Result.error("用户未登录");
        }

        try {
            FileEntity fileEntity = fileService.uploadFile(file, user.getUserid(), projectId, taskId, folderId);
            return Result.success(fileEntity);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }

    @Operation(summary = "根据ID获取文件信息")
    @GetMapping("/{fileId}")
    public Result<FileEntity> getFileById(
            @PathVariable Long fileId,
            HttpServletRequest request) {

        User user = getCurrentUser(request);
        if (user == null) {
            return Result.error("用户未登录");
        }

        FileEntity file = fileService.getFileById(fileId, user.getUserid());
        if (file == null) {
            return Result.error("文件不存在或无权限访问");
        }

        return Result.success(file);
    }

    @Operation(summary = "根据项目获取文件列表")
    @GetMapping("/project/{projectId}")
    public Result<List<FileEntity>> getFilesByProject(
            @PathVariable Long projectId,
            HttpServletRequest request) {

        User user = getCurrentUser(request);
        if (user == null) {
            return Result.error("用户未登录");
        }

        List<FileEntity> files = fileService.getFilesByProject(projectId, user.getUserid());
        return Result.success(files);
    }

    @Operation(summary = "根据任务获取文件列表")
    @GetMapping("/task/{taskId}")
    public Result<List<FileEntity>> getFilesByTask(
            @PathVariable Long taskId,
            HttpServletRequest request) {

        User user = getCurrentUser(request);
        if (user == null) {
            return Result.error("用户未登录");
        }

        List<FileEntity> files = fileService.getFilesByTask(taskId, user.getUserid());
        return Result.success(files);
    }

    @Operation(summary = "根据文件夹获取文件列表")
    @GetMapping("/folder/{folderId}")
    public Result<List<FileEntity>> getFilesByFolder(
            @PathVariable Long folderId,
            HttpServletRequest request) {

        User user = getCurrentUser(request);
        if (user == null) {
            return Result.error("用户未登录");
        }

        List<FileEntity> files = fileService.getFilesByFolder(folderId, user.getUserid());
        return Result.success(files);
    }

    @Operation(summary = "下载文件")
    @GetMapping("/{fileId}/download")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable Long fileId,
            HttpServletRequest request) {

        User user = getCurrentUser(request);
        if (user == null) {
            return ResponseEntity.badRequest().build();
        }

        try {
            FileEntity fileEntity = fileService.getFileById(fileId, user.getUserid());
            if (fileEntity == null) {
                return ResponseEntity.notFound().build();
            }

            Path filePath = Paths.get(fileEntity.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                String contentType = Files.probeContentType(filePath);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "attachment; filename=\"" + fileEntity.getOriginalName() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            log.error("文件下载失败", e);
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            log.error("文件下载失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "获取文件预览")
    @GetMapping("/{fileId}/preview")
    public Result<Map<String, Object>> getFilePreview(
            @PathVariable Long fileId,
            HttpServletRequest request) {

        User user = getCurrentUser(request);
        if (user == null) {
            return Result.error("用户未登录");
        }

        Map<String, Object> preview = fileService.getFilePreview(fileId, user.getUserid());
        return Result.success(preview);
    }

    @Operation(summary = "删除文件")
    @DeleteMapping("/{fileId}")
    public Result<Void> deleteFile(
            @PathVariable Long fileId,
            HttpServletRequest request) {

        User user = getCurrentUser(request);
        if (user == null) {
            return Result.error("用户未登录");
        }

        try {
            fileService.deleteFile(fileId, user.getUserid());
            return Result.success();
        } catch (Exception e) {
            log.error("删除文件失败", e);
            return Result.error("删除文件失败: " + e.getMessage());
        }
    }

    @Operation(summary = "更新文件信息")
    @PutMapping("/{fileId}")
    public Result<FileEntity> updateFileInfo(
            @PathVariable Long fileId,
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {

        User user = getCurrentUser(httpRequest);
        if (user == null) {
            return Result.error("用户未登录");
        }

        String name = request.get("name");
        String description = request.get("description");

        try {
            FileEntity fileEntity = fileService.updateFileInfo(fileId, name, description, user.getUserid());
            return Result.success(fileEntity);
        } catch (Exception e) {
            log.error("更新文件信息失败", e);
            return Result.error("更新文件信息失败: " + e.getMessage());
        }
    }

    @Operation(summary = "移动文件")
    @PostMapping("/{fileId}/move")
    public Result<Void> moveFile(
            @PathVariable Long fileId,
            @RequestBody Map<String, Long> request,
            HttpServletRequest httpRequest) {

        User user = getCurrentUser(httpRequest);
        if (user == null) {
            return Result.error("用户未登录");
        }

        Long targetFolderId = request.get("targetFolderId");

        try {
            fileService.moveFile(fileId, targetFolderId, user.getUserid());
            return Result.success();
        } catch (Exception e) {
            log.error("移动文件失败", e);
            return Result.error("移动文件失败: " + e.getMessage());
        }
    }

    @Operation(summary = "复制文件")
    @PostMapping("/{fileId}/copy")
    public Result<FileEntity> copyFile(
            @PathVariable Long fileId,
            @RequestBody Map<String, Long> request,
            HttpServletRequest httpRequest) {

        User user = getCurrentUser(httpRequest);
        if (user == null) {
            return Result.error("用户未登录");
        }

        Long targetFolderId = request.get("targetFolderId");

        try {
            FileEntity fileEntity = fileService.copyFile(fileId, targetFolderId, user.getUserid());
            return Result.success(fileEntity);
        } catch (Exception e) {
            log.error("复制文件失败", e);
            return Result.error("复制文件失败: " + e.getMessage());
        }
    }

    @Operation(summary = "批量删除文件")
    @DeleteMapping("/batch")
    public Result<Void> batchDeleteFiles(
            @RequestBody Map<String, List<Long>> request,
            HttpServletRequest httpRequest) {

        User user = getCurrentUser(httpRequest);
        if (user == null) {
            return Result.error("用户未登录");
        }

        List<Long> fileIds = request.get("fileIds");
        if (fileIds == null || fileIds.isEmpty()) {
            return Result.error("文件ID列表不能为空");
        }

        try {
            fileService.batchDeleteFiles(fileIds, user.getUserid());
            return Result.success();
        } catch (Exception e) {
            log.error("批量删除文件失败", e);
            return Result.error("批量删除文件失败: " + e.getMessage());
        }
    }

    @Operation(summary = "搜索文件")
    @GetMapping("/search")
    public Result<List<FileEntity>> searchFiles(
            @RequestParam String keyword,
            HttpServletRequest request) {

        User user = getCurrentUser(request);
        if (user == null) {
            return Result.error("用户未登录");
        }

        List<FileEntity> files = fileService.searchFiles(keyword, user.getUserid());
        return Result.success(files);
    }

    @Operation(summary = "获取文件统计信息")
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getFileStatistics(HttpServletRequest request) {
        User user = getCurrentUser(request);
        if (user == null) {
            return Result.error("用户未登录");
        }

        Map<String, Object> statistics = fileService.getFileStatistics(user.getUserid());
        return Result.success(statistics);
    }

    @Operation(summary = "获取文件URL")
    @GetMapping("/{fileId}/url")
    public Result<String> getFileUrl(@PathVariable Long fileId) {
        String url = fileService.getFileUrl(fileId);
        return Result.success(url);
    }

    /**
     * 获取当前登录用户
     */
    private User getCurrentUser(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return userService.getUserByToken(token);
    }
}