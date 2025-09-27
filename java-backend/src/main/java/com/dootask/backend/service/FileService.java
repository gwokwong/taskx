package com.dootask.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dootask.backend.entity.FileEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileService extends IService<FileEntity> {

    FileEntity uploadFile(MultipartFile file, Long userId, Long projectId, Long taskId, Long folderId);

    FileEntity getFileById(Long fileId, Long userId);

    List<FileEntity> getFilesByFolder(Long folderId, Long userId);

    List<FileEntity> getFilesByProject(Long projectId, Long userId);

    void deleteFile(Long fileId, Long userId);

    byte[] downloadFile(Long fileId, Long userId);

    String getFileUrl(Long fileId);
}