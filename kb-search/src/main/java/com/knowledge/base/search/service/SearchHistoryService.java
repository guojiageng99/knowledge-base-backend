package com.knowledge.base.search.service;

import com.knowledge.base.search.vo.SearchHistoryVO;

import java.util.List;

public interface SearchHistoryService {
    void save(Long userId, String keyword);
    List<SearchHistoryVO> list(Long userId);
    List<String> hot();
    boolean clear(Long userId);
}
