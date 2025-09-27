package com.dootask.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dootask.backend.entity.Dialog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface DialogMapper extends BaseMapper<Dialog> {

    @Select("SELECT d.*, " +
            "(SELECT content FROM messages WHERE dialog_id = d.id ORDER BY created_at DESC LIMIT 1) as last_msg, " +
            "(SELECT created_at FROM messages WHERE dialog_id = d.id ORDER BY created_at DESC LIMIT 1) as last_at, " +
            "(SELECT COUNT(*) FROM messages m WHERE m.dialog_id = d.id AND m.created_at > COALESCE(dm.last_read_at, '1970-01-01')) as unread_count " +
            "FROM dialogs d " +
            "INNER JOIN dialog_members dm ON d.id = dm.dialog_id " +
            "WHERE dm.user_id = #{userId} " +
            "ORDER BY last_at DESC")
    List<Map<String, Object>> getUserDialogs(Long userId);
}