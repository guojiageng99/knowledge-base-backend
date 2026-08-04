package com.knowledge.base.search.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.knowledge.base.common.result.PageResult;
import com.knowledge.base.common.result.Result;
import com.knowledge.base.search.dto.SearchRequestDTO;
import com.knowledge.base.search.entity.DocumentIndex;
import com.knowledge.base.search.feign.RagSearchFeignClient;
import com.knowledge.base.search.feign.RagSearchItemVO;
import com.knowledge.base.search.feign.RagSearchRequest;
import com.knowledge.base.search.service.SearchService;
import com.knowledge.base.search.vo.SearchResultVO;
import com.knowledge.base.search.vo.SearchSuggestVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {
    private final ElasticsearchClient client;
    private final RagSearchFeignClient ragClient;

    @Value("${search.document-index:kb_document}") private String indexName;

    @Override
    public PageResult<SearchResultVO> search(SearchRequestDTO request) {
        return "hybrid".equalsIgnoreCase(request.getSearchMode()) ? hybrid(request) : keyword(request);
    }

    @Override
    public List<SearchSuggestVO> suggest(String keyword, Integer size) {
        if (keyword == null || keyword.isBlank()) return List.of();
        try {
            int limit = clamp(size, 1, 20, 10);
            SearchResponse<DocumentIndex> response = client.search(search -> search.index(indexName).size(limit)
                    .query(query -> query.multiMatch(match -> match.query(keyword).fields("title^3", "creatorName", "categoryName"))), DocumentIndex.class);
            return response.hits().hits().stream().filter(hit -> hit.source() != null).map(hit -> SearchSuggestVO.builder()
                    .text(hit.source().getTitle()).type("title").documentId(toLong(hit.id())).score(hit.score() == null ? 0D : hit.score().doubleValue()).build()).toList();
        } catch (IOException exception) { log.warn("Search suggestions unavailable", exception); return List.of(); }
    }

    @Override
    public void indexDocumentData(Map<String, Object> data) {
        if (data == null || toLong(data.get("id")) == null) return;
        DocumentIndex document = document(data);
        try { createIndexIfMissing(); client.index(index -> index.index(indexName).id(document.getId()).document(document)); client.indices().refresh(refresh -> refresh.index(indexName)); }
        catch (IOException exception) { throw new IllegalStateException("Cannot index document", exception); }
    }

    @Override
    public void deleteDocument(Long documentId) {
        if (documentId == null) return;
        try { if (client.indices().exists(exists -> exists.index(indexName)).value()) client.delete(delete -> delete.index(indexName).id(documentId.toString())); }
        catch (IOException exception) { throw new IllegalStateException("Cannot delete document index", exception); }
    }

    @Override
    public void rebuildIndex() {
        try {
            if (client.indices().exists(exists -> exists.index(indexName)).value()) client.indices().delete(delete -> delete.index(indexName));
            createIndexIfMissing();
        } catch (IOException exception) { throw new IllegalStateException("Cannot rebuild document search index", exception); }
    }

    private PageResult<SearchResultVO> keyword(SearchRequestDTO request) {
        int current = clamp(request.getCurrent(), 1, Integer.MAX_VALUE, 1), size = clamp(request.getSize(), 1, 100, 10);
        try {
            SearchResponse<DocumentIndex> response = client.search(search -> search.index(indexName).from((current - 1) * size).size(size)
                    .query(query -> query.bool(bool -> {
                        bool.must(must -> must.multiMatch(match -> match.query(request.getKeyword()).fields("title^4", "summary^2", "content")));
                        bool.filter(filter -> filter.term(term -> term.field("docStatus").value(request.getDocStatus() == null ? 1 : request.getDocStatus())));
                        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) bool.filter(filter -> filter.terms(terms -> terms.field("categoryId").terms(values -> values.value(request.getCategoryIds().stream().map(id -> co.elastic.clients.elasticsearch._types.FieldValue.of(id)).toList()))));
                        if (request.getCreatorId() != null) bool.filter(filter -> filter.term(term -> term.field("creatorId").value(request.getCreatorId())));
                        return bool;
                    })).highlight(highlight -> highlight.fields("title", field -> field.preTags("<em>").postTags("</em>")).fields("summary", field -> field.preTags("<em>").postTags("</em>")).fields("content", field -> field.preTags("<em>").postTags("</em>")).fragmentSize(180).numberOfFragments(2)), DocumentIndex.class);
            List<SearchResultVO> records = response.hits().hits().stream().filter(hit -> hit.source() != null).map(this::keywordResult).toList();
            long total = response.hits().total() == null ? records.size() : response.hits().total().value();
            return PageResult.of(current, size, total, records);
        } catch (IOException exception) { log.warn("Keyword search unavailable", exception); return PageResult.empty(current, size); }
    }

    private PageResult<SearchResultVO> hybrid(SearchRequestDTO request) {
        RagSearchRequest ragRequest = new RagSearchRequest(); ragRequest.setQuery(request.getKeyword()); ragRequest.setTopK(clamp(request.getTopK(), 1, 50, 10)); ragRequest.setEnableRerank(request.isEnableRerank());
        Result<List<RagSearchItemVO>> response = ragClient.search(ragRequest);
        if (response == null || response.getData() == null) return PageResult.empty(1, request.getTopK());
        Map<Long, List<RagSearchItemVO>> grouped = new LinkedHashMap<>();
        for (RagSearchItemVO item : response.getData()) if (item.getDocumentId() != null) grouped.computeIfAbsent(item.getDocumentId(), ignored -> new ArrayList<>()).add(item);
        List<SearchResultVO> records = grouped.entrySet().stream().map(entry -> hybridResult(entry.getKey(), entry.getValue())).sorted(Comparator.comparing(SearchResultVO::getScore).reversed()).toList();
        return PageResult.of(1, request.getTopK(), records.size(), records);
    }

    private SearchResultVO keywordResult(Hit<DocumentIndex> hit) {
        DocumentIndex document = hit.source(); Map<String, List<String>> highlights = hit.highlight();
        String title = first(highlights.get("title"), document.getTitle()); String summary = first(highlights.get("summary"), document.getSummary());
        List<String> fragments = highlights.getOrDefault("content", List.of());
        return SearchResultVO.builder().id(toLong(document.getId())).title(title).summary(summary).highlights(fragments).categoryName(document.getCategoryName()).tags(document.getTags()).creatorName(document.getCreatorName()).viewCount(document.getViewCount()).likeCount(document.getLikeCount()).commentCount(document.getCommentCount()).publishAt(document.getPublishAt()).score(hit.score() == null ? 0D : hit.score().doubleValue()).build();
    }

    private SearchResultVO hybridResult(Long documentId, List<RagSearchItemVO> items) {
        RagSearchItemVO first = items.getFirst(); List<SearchResultVO.ChunkResult> chunks = items.stream().map(item -> SearchResultVO.ChunkResult.builder().chunkId(item.getChunkId()).content(highlight(item.getContent())).heading(item.getHeading()).score(item.getScore()).bm25Score(item.getBm25Score()).vectorScore(item.getVectorScore()).build()).toList();
        return SearchResultVO.builder().id(documentId).title(first.getDocumentTitle()).summary(first.getContent()).highlights(chunks.stream().map(SearchResultVO.ChunkResult::getContent).toList()).score(first.getScore()).bm25Score(first.getBm25Score()).vectorScore(first.getVectorScore()).chunks(chunks).build();
    }

    private void createIndexIfMissing() throws IOException {
        if (client.indices().exists(exists -> exists.index(indexName)).value()) return;
        client.indices().create(create -> create.index(indexName).settings(settings -> settings.numberOfShards("1").numberOfReplicas("0"))
                .mappings(mapping -> mapping.properties("title", field -> field.text(text -> text.fields("keyword", keyword -> keyword.keyword(keywordField -> keywordField))))
                        .properties("summary", field -> field.text(text -> text)).properties("content", field -> field.text(text -> text)).properties("categoryId", field -> field.long_(number -> number))
                        .properties("creatorId", field -> field.long_(number -> number)).properties("docStatus", field -> field.integer(number -> number))));
    }

    private DocumentIndex document(Map<String, Object> data) { Long id = toLong(data.get("id")); return DocumentIndex.builder().id(id.toString()).title(string(data.get("title"))).summary(string(data.get("summary"))).content(string(data.get("content"))).categoryId(toLong(data.get("categoryId"))).categoryName(string(data.get("categoryName"))).tags(string(data.get("tags"))).creatorId(toLong(data.get("authorId"))).creatorName(string(data.get("authorName"))).docStatus(toInteger(data.get("status"))).viewCount(toLong(data.get("viewCount"))).likeCount(toLong(data.get("likeCount"))).commentCount(toLong(data.get("commentCount"))).isPublic(toInteger(data.get("isPublic")) == 1).publishAt(string(data.get("publishTime"))).createdAt(string(data.get("createTime"))).updatedAt(string(data.get("updateTime"))).build(); }
    private int clamp(Integer value, int min, int max, int fallback) { return value == null ? fallback : Math.min(Math.max(value, min), max); }
    private String first(List<String> values, String fallback) { return values == null || values.isEmpty() ? fallback : values.getFirst(); }
    private String highlight(String value) { return value == null ? null : value; }
    private String string(Object value) { return value == null ? null : value.toString(); }
    private Long toLong(Object value) { return value instanceof Number number ? number.longValue() : value == null ? null : Long.valueOf(value.toString()); }
    private Integer toInteger(Object value) { return value instanceof Number number ? number.intValue() : value == null ? null : Integer.valueOf(value.toString()); }
}
