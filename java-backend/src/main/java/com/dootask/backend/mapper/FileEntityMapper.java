package com.dootask.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dootask.backend.entity.FileEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 文件实体 Mapper 接口
 */
@Mapper
public interface FileEntityMapper extends BaseMapper<FileEntity> {

    /**
     * 根据项目ID获取文件列表
     */
    @Select("SELECT f.*, u.nickname as owner_nickname, u.user_img as owner_avatar " +
            "FROM file_entities f " +
            "LEFT JOIN users u ON f.owner_id = u.userid " +
            "WHERE f.project_id = #{projectId} AND f.status = 'active' " +
            "ORDER BY f.created_at DESC")
    List<FileEntity> getFilesByProjectId(@Param("projectId") Long projectId);

    /**
     * 根据任务ID获取文件列表
     */
    @Select("SELECT f.*, u.nickname as owner_nickname, u.user_img as owner_avatar " +
            "FROM file_entities f " +
            "LEFT JOIN users u ON f.owner_id = u.userid " +
            "WHERE f.task_id = #{taskId} AND f.status = 'active' " +
            "ORDER BY f.created_at DESC")
    List<FileEntity> getFilesByTaskId(@Param("taskId") Long taskId);

    /**
     * 根据文件夹ID获取文件列表
     */
    @Select("SELECT f.*, u.nickname as owner_nickname, u.user_img as owner_avatar " +
            "FROM file_entities f " +
            "LEFT JOIN users u ON f.owner_id = u.userid " +
            "WHERE f.folder_id = #{folderId} AND f.status = 'active' " +
            "ORDER BY f.created_at DESC")
    List<FileEntity> getFilesByFolderId(@Param("folderId") Long folderId);

    /**
     * 根据所有者ID获取文件列表
     */
    @Select("SELECT f.*, u.nickname as owner_nickname, u.user_img as owner_avatar " +
            "FROM file_entities f " +
            "LEFT JOIN users u ON f.owner_id = u.userid " +
            "WHERE f.owner_id = #{ownerId} AND f.status = 'active' " +
            "ORDER BY f.created_at DESC")
    List<FileEntity> getFilesByOwnerId(@Param("ownerId") Long ownerId);

    /**
     * 搜索文件
     */
    @Select("SELECT f.*, u.nickname as owner_nickname, u.user_img as owner_avatar " +
            "FROM file_entities f " +
            "LEFT JOIN users u ON f.owner_id = u.userid " +
            "WHERE f.name LIKE CONCAT('%', #{keyword}, '%') AND f.status = 'active' " +
            "ORDER BY f.created_at DESC")
    List<FileEntity> searchFiles(@Param("keyword") String keyword);

    /**
     * 获取文件统计信息
     */
    @Select("SELECT " +
            "COUNT(*) as total_files, " +
            "SUM(size) as total_size, " +
            "COUNT(DISTINCT owner_id) as unique_owners " +
            "FROM file_entities " +
            "WHERE status = 'active'")
    Object getFileStatistics();
}