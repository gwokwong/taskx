package com.dootask.backend.controller;

import com.dootask.backend.common.Result;
import com.dootask.backend.entity.FileEntity;
import com.dootask.backend.service.FileService;
import com.dootask.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

@Tag(name = "文件管理", description = "文件相关接口")
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
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

        Long userId = getCurrentUserId(request);
        FileEntity uploadedFile = fileService.uploadFile(file, userId, projectId, taskId, folderId);
        return Result.success("上传成功", uploadedFile);
    }

    @Operation(summary = "获取文件列表")
    @GetMapping
    public Result<List<FileEntity>> getFiles(
            @RequestParam(value = "folderId", required = false) Long folderId,
            @RequestParam(value = "projectId", required = false) Long projectId,
            HttpServletRequest request) {

        Long userId = getCurrentUserId(request);
        List<FileEntity> files;

        if (projectId != null) {
            files = fileService.getFilesByProject(projectId, userId);
        } else {
            files = fileService.getFilesByFolder(folderId, userId);
        }

        return Result.success(files);
    }

    @Operation(summary = "获取文件详情")
    @GetMapping("/{id}")
    public Result<FileEntity> getFile(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        FileEntity file = fileService.getFileById(id, userId);
        return Result.success(file);
    }

    @Operation(summary = "下载文件")
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        FileEntity file = fileService.getFileById(id, userId);
        byte[] fileContent = fileService.downloadFile(id, userId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        try {
            String encodedFilename = URLEncoder.encode(file.getOriginalName(), "UTF-8");
            headers.setContentDispositionFormData("attachment", encodedFilename);
        } catch (UnsupportedEncodingException e) {
            headers.setContentDispositionFormData("attachment", file.getFileName());
        }

        return ResponseEntity.ok()
                .headers(headers)
                .body(fileContent);
    }

    @Operation(summary = "删除文件")
    @DeleteMapping("/{id}")
    public Result<Void> deleteFile(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        fileService.deleteFile(id, userId);
        return Result.success("删除成功");
    }

    @Operation(summary = "获取文件访问URL")
    @GetMapping("/{id}/url")
    public Result<String> getFileUrl(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        // 验证权限
        fileService.getFileById(id, userId);
        String url = fileService.getFileUrl(id);
        return Result.success(url);
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