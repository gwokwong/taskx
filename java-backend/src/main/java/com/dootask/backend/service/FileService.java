package com.dootask.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dootask.backend.entity.FileEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface FileService extends IService<FileEntity> {

    /**
     * 上传文件
     */
    FileEntity uploadFile(MultipartFile file, Long userId, Long projectId, Long taskId, Long folderId) throws IOException;

    /**
     * 根据ID获取文件信息
     */
    FileEntity getFileById(Long fileId, Long userId);

    /**
     * 根据文件夹获取文件列表
     */
    List<FileEntity> getFilesByFolder(Long folderId, Long userId);

    /**
     * 根据项目获取文件列表
     */
    List<FileEntity> getFilesByProject(Long projectId, Long userId);

    /**
     * 根据任务获取文件列表
     */
    List<FileEntity> getFilesByTask(Long taskId, Long userId);

    /**
     * 删除文件
     */
    void deleteFile(Long fileId, Long userId);

    /**
     * 下载文件
     */
    byte[] downloadFile(Long fileId, Long userId) throws IOException;

    /**
     * 获取文件URL
     */
    String getFileUrl(Long fileId);

    /**
     * 搜索文件
     */
    List<FileEntity> searchFiles(String keyword, Long userId);

    /**
     * 获取文件预览信息
     */
    Map<String, Object> getFilePreview(Long fileId, Long userId);

    /**
     * 更新文件信息
     */
    FileEntity updateFileInfo(Long fileId, String name, String description, Long userId);

    /**
     * 移动文件
     */
    void moveFile(Long fileId, Long targetFolderId, Long userId);

    /**
     * 复制文件
     */
    FileEntity copyFile(Long fileId, Long targetFolderId, Long userId) throws IOException;

    /**
     * 批量删除文件
     */
    void batchDeleteFiles(List<Long> fileIds, Long userId);

    /**
     * 获取文件统计信息
     */
    Map<String, Object> getFileStatistics(Long userId);

    /**
     * 检查文件是否存在
     */
    boolean fileExists(String hash);

    /**
     * 根据哈希值获取文件
     */
    FileEntity getFileByHash(String hash);
}