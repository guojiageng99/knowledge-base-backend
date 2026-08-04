package com.knowledge.base.search.controller;

import com.knowledge.base.common.result.PageResult;
import com.knowledge.base.common.result.Result;
import com.knowledge.base.search.dto.SearchRequestDTO;
import com.knowledge.base.search.service.SearchHistoryService;
import com.knowledge.base.search.service.SearchService;
import com.knowledge.base.search.vo.SearchHistoryVO;
import com.knowledge.base.search.vo.SearchResultVO;
import com.knowledge.base.search.vo.SearchSuggestVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class SearchController {
    private final SearchService searchService;
    private final SearchHistoryService historyService;

    @PostMapping public Result<PageResult<SearchResultVO>> search(@Valid @RequestBody SearchRequestDTO request, HttpServletRequest servletRequest) {
        Long userId = userId(servletRequest); String keyword = request.getKeyword();
        PageResult<SearchResultVO> result = searchService.search(request);
        CompletableFuture.runAsync(() -> historyService.save(userId, keyword));
        return Result.success(result);
    }
    @GetMapping("/suggest") public Result<List<SearchSuggestVO>> suggest(@RequestParam String keyword, @RequestParam(defaultValue = "10") Integer size) { return Result.success(searchService.suggest(keyword, size)); }
    @GetMapping("/hot") public Result<List<String>> hot() { return Result.success(historyService.hot()); }
    @GetMapping("/history") public Result<List<SearchHistoryVO>> history(HttpServletRequest request) { return Result.success(historyService.list(userId(request))); }
    @DeleteMapping("/history") public Result<Boolean> clear(HttpServletRequest request) { return Result.success(historyService.clear(userId(request))); }
    @PostMapping("/index/rebuild") public Result<Void> rebuild() { searchService.rebuildIndex(); return Result.success(); }
    @PostMapping("/internal/index/document") public Result<Void> index(@RequestBody Map<String, Object> data) { searchService.indexDocumentData(data); return Result.success(); }
    @DeleteMapping("/internal/index/document/{documentId}") public Result<Void> delete(@PathVariable Long documentId) { searchService.deleteDocument(documentId); return Result.success(); }

    private Long userId(HttpServletRequest request) { try { return Long.valueOf(request.getHeader("X-User-Id")); } catch (Exception ignored) { return 1L; } }
}
