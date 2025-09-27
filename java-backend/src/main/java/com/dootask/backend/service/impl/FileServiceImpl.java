package com.dootask.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dootask.backend.common.exception.ApiException;
import com.dootask.backend.entity.FileEntity;
import com.dootask.backend.mapper.FileMapper;
import com.dootask.backend.service.FileService;
import com.dootask.backend.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileServiceImpl extends ServiceImpl<FileMapper, FileEntity> implements FileService {

    private final ProjectService projectService;

    @Value("${app.file.upload-path:/data/uploads/}")
    private String uploadPath;

    @Override
    public FileEntity uploadFile(MultipartFile file, Long userId, Long projectId, Long taskId, Long folderId) {
        if (file.isEmpty()) {
            throw new ApiException("文件不能为空");
        }

        // 验证项目权限
        if (projectId != null) {
            projectService.getProjectById(projectId, userId);
        }

        try {
            // 创建上传目录
            Path uploadDir = Paths.get(uploadPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID().toString() + fileExtension;

            // 保存文件
            Path filePath = uploadDir.resolve(fileName);
            Files.copy(file.getInputStream(), filePath);

            // 保存文件信息到数据库
            FileEntity fileEntity = new FileEntity();
            fileEntity.setFileName(fileName);
            fileEntity.setOriginalName(originalFilename);
            fileEntity.setFilePath(filePath.toString());
            fileEntity.setFileSize(file.getSize());
            fileEntity.setFileType(getFileType(originalFilename));
            fileEntity.setMimeType(file.getContentType());
            fileEntity.setUploadUserid(userId);
            fileEntity.setProjectId(projectId);
            fileEntity.setTaskId(taskId);
            fileEntity.setFolderId(folderId != null ? folderId : 0L);
            fileEntity.setCreatedAt(LocalDateTime.now());
            fileEntity.setUpdatedAt(LocalDateTime.now());

            save(fileEntity);
            return fileEntity;

        } catch (IOException e) {
            throw new ApiException("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public FileEntity getFileById(Long fileId, Long userId) {
        FileEntity file = getById(fileId);
        if (file == null) {
            throw new ApiException("文件不存在");
        }

        // 验证权限
        if (!hasFilePermission(file, userId)) {
            throw new ApiException("无权限访问此文件");
        }

        return file;
    }

    @Override
    public List<FileEntity> getFilesByFolder(Long folderId, Long userId) {
        return list(new QueryWrapper<FileEntity>()
                .eq("folder_id", folderId != null ? folderId : 0L)
                .eq("upload_userid", userId)
                .orderByDesc("created_at"));
    }

    @Override
    public List<FileEntity> getFilesByProject(Long projectId, Long userId) {
        // 验证项目权限
        projectService.getProjectById(projectId, userId);

        return list(new QueryWrapper<FileEntity>()
                .eq("project_id", projectId)
                .orderByDesc("created_at"));
    }

    @Override
    public void deleteFile(Long fileId, Long userId) {
        FileEntity file = getFileById(fileId, userId);

        try {
            // 删除物理文件
            Path filePath = Paths.get(file.getFilePath());
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
        } catch (IOException e) {
            // 记录日志但不抛出异常
            log.error("删除物理文件失败: " + e.getMessage());
        }

        // 删除数据库记录
        removeById(fileId);
    }

    @Override
    public byte[] downloadFile(Long fileId, Long userId) {
        FileEntity file = getFileById(fileId, userId);

        try {
            Path filePath = Paths.get(file.getFilePath());
            if (!Files.exists(filePath)) {
                throw new ApiException("文件不存在");
            }

            // 更新下载次数
            file.setDownloadCount(file.getDownloadCount() + 1);
            updateById(file);

            return Files.readAllBytes(filePath);

        } catch (IOException e) {
            throw new ApiException("文件下载失败: " + e.getMessage());
        }
    }

    @Override
    public String getFileUrl(Long fileId) {
        FileEntity file = getById(fileId);
        if (file == null) {
            return null;
        }
        // 返回文件访问URL
        return "/api/files/" + fileId + "/download";
    }

    private boolean hasFilePermission(FileEntity file, Long userId) {
        // 文件上传者可以访问
        if (file.getUploadUserid().equals(userId)) {
            return true;
        }

        // 项目成员可以访问项目文件
        if (file.getProjectId() != null) {
            try {
                projectService.getProjectById(file.getProjectId(), userId);
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        // 公开文件可以访问
        return !file.getIsPrivate();
    }

    private String getFileType(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return "unknown";
        }

        String extension = "";
        if (fileName.contains(".")) {
            extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        }

        switch (extension) {
            case "jpg":
            case "jpeg":
            case "png":
            case "gif":
            case "bmp":
                return "image";
            case "mp4":
            case "avi":
            case "mov":
            case "wmv":
                return "video";
            case "mp3":
            case "wav":
            case "flac":
                return "audio";
            case "pdf":
                return "pdf";
            case "doc":
            case "docx":
                return "word";
            case "xls":
            case "xlsx":
                return "excel";
            case "ppt":
            case "pptx":
                return "powerpoint";
            case "txt":
                return "text";
            case "zip":
            case "rar":
            case "7z":
                return "archive";
            default:
                return "unknown";
        }
    }
}