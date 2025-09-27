package com.dootask.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dootask.backend.entity.SearchLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SearchLogMapper extends BaseMapper<SearchLog> {

    @Select("SELECT DISTINCT keyword FROM search_logs WHERE user_id = #{userId} ORDER BY search_at DESC LIMIT 10")
    List<String> getRecentSearches(Long userId);

    @Select("SELECT keyword, COUNT(*) as count FROM search_logs " +
            "WHERE keyword LIKE CONCAT('%', #{keyword}, '%') " +
            "GROUP BY keyword " +
            "ORDER BY count DESC, MAX(search_at) DESC " +
            "LIMIT 5")
    List<String> getSearchSuggestions(String keyword);
}