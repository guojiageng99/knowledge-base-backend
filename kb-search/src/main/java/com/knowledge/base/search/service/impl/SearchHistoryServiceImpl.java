package com.knowledge.base.search.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.knowledge.base.search.entity.SearchHistory;
import com.knowledge.base.search.mapper.SearchHistoryMapper;
import com.knowledge.base.search.service.SearchHistoryService;
import com.knowledge.base.search.vo.SearchHistoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchHistoryServiceImpl implements SearchHistoryService {
    private final SearchHistoryMapper mapper;

    @Override
    public void save(Long userId, String keyword) {
        if (userId == null || keyword == null || keyword.isBlank()) return;
        SearchHistory existing = mapper.selectOne(new LambdaQueryWrapper<SearchHistory>().eq(SearchHistory::getUserId, userId).eq(SearchHistory::getKeyword, keyword));
        if (existing == null) {
            SearchHistory history = new SearchHistory();
            history.setUserId(userId); history.setKeyword(keyword); history.setSearchCount(1); history.setCreateTime(LocalDateTime.now()); mapper.insert(history);
        } else {
            existing.setSearchCount(existing.getSearchCount() + 1); existing.setCreateTime(LocalDateTime.now()); mapper.updateById(existing);
        }
    }

    @Override
    public List<SearchHistoryVO> list(Long userId) {
        if (userId == null) return List.of();
        return mapper.selectList(new LambdaQueryWrapper<SearchHistory>().eq(SearchHistory::getUserId, userId).orderByDesc(SearchHistory::getCreateTime).last("LIMIT 20"))
                .stream().map(value -> SearchHistoryVO.builder().id(value.getId()).keyword(value.getKeyword()).searchCount(value.getSearchCount()).createTime(value.getCreateTime()).build()).toList();
    }

    @Override
    public List<String> hot() {
        return mapper.selectList(new LambdaQueryWrapper<SearchHistory>().ge(SearchHistory::getCreateTime, LocalDateTime.now().minusDays(7)).orderByDesc(SearchHistory::getSearchCount).last("LIMIT 10"))
                .stream().map(SearchHistory::getKeyword).distinct().toList();
    }

    @Override
    public boolean clear(Long userId) {
        return userId != null && mapper.delete(new LambdaQueryWrapper<SearchHistory>().eq(SearchHistory::getUserId, userId)) > 0;
    }
}
