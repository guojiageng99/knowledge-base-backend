package com.knowledge.base.search.service;

import com.knowledge.base.common.result.PageResult;
import com.knowledge.base.search.dto.SearchRequestDTO;
import com.knowledge.base.search.vo.SearchResultVO;
import com.knowledge.base.search.vo.SearchSuggestVO;

import java.util.List;
import java.util.Map;

public interface SearchService {
    PageResult<SearchResultVO> search(SearchRequestDTO request);
    List<SearchSuggestVO> suggest(String keyword, Integer size);
    void indexDocumentData(Map<String, Object> data);
    void deleteDocument(Long documentId);
    void rebuildIndex();
}
