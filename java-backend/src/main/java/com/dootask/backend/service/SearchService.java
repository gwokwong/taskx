package com.dootask.backend.service;

import com.dootask.backend.dto.SearchResultDto;

import java.util.List;

public interface SearchService {

    List<SearchResultDto> globalSearch(String keyword, Long userId, String type);

    List<SearchResultDto> searchProjects(String keyword, Long userId);

    List<SearchResultDto> searchTasks(String keyword, Long userId);

    List<SearchResultDto> searchFiles(String keyword, Long userId);

    List<SearchResultDto> searchUsers(String keyword, Long userId);

    List<String> getSearchSuggestions(String keyword, Long userId);

    List<String> getRecentSearches(Long userId);

    void logSearch(Long userId, String keyword, String type, Integer resultCount);
}