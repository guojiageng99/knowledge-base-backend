package com.knowledge.base.ai.rag.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.knowledge.base.ai.rag.config.RagProperties;
import com.knowledge.base.ai.rag.entity.DocumentChunk;
import com.knowledge.base.ai.rag.entity.KbChunkDoc;
import com.knowledge.base.ai.rag.service.VectorIndexService;
import com.knowledge.base.ai.rag.vo.RagSearchResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class VectorIndexServiceImpl implements VectorIndexService {
    private final ElasticsearchClient client;
    private final RagProperties properties;

    @Override
    public boolean indexExists() {
        try { return client.indices().exists(request -> request.index(properties.getIndex().getName())).value(); }
        catch (IOException exception) { throw new IllegalStateException("Elasticsearch unavailable", exception); }
    }

    @Override
    public void createIndexIfNotExists() {
        if (indexExists()) return;
        try {
            client.indices().create(request -> request.index(properties.getIndex().getName())
                    .settings(settings -> settings.numberOfShards(String.valueOf(properties.getIndex().getShards()))
                            .numberOfReplicas(String.valueOf(properties.getIndex().getReplicas())))
                    .mappings(mappings -> mappings
                            .properties("chunkId", property -> property.keyword(keyword -> keyword))
                            .properties("documentId", property -> property.long_(number -> number))
                            .properties("documentTitle", property -> property.text(text -> text))
                            .properties("content", property -> property.text(text -> text))
                            .properties("heading", property -> property.keyword(keyword -> keyword))
                            .properties("chunkIndex", property -> property.integer(number -> number))
                            .properties("totalChunks", property -> property.integer(number -> number))
                            .properties("categoryId", property -> property.long_(number -> number))
                            .properties("authorId", property -> property.long_(number -> number))
                            .properties("docStatus", property -> property.integer(number -> number))
                            .properties("embedding", property -> property.denseVector(vector -> vector
                                    .dims(properties.getEmbedding().getDimension()).index(true).similarity("cosine")))));
        } catch (IOException exception) { throw new IllegalStateException("Cannot create RAG index", exception); }
    }

    @Override
    public void indexChunks(List<DocumentChunk> chunks) {
        if (chunks == null || chunks.isEmpty()) return;
        createIndexIfNotExists();
        try {
            for (DocumentChunk chunk : chunks) client.index(request -> request.index(properties.getIndex().getName()).id(chunk.getChunkId()).document(chunk));
            client.indices().refresh(request -> request.index(properties.getIndex().getName()));
        } catch (IOException exception) { throw new IllegalStateException("Cannot index document chunks", exception); }
    }

    @Override
    public void deleteByDocId(Long documentId) {
        if (!indexExists()) return;
        try { client.deleteByQuery(request -> request.index(properties.getIndex().getName()).query(query -> query.term(term -> term.field("documentId").value(documentId)))); }
        catch (IOException exception) { throw new IllegalStateException("Cannot delete document index", exception); }
    }

    @Override
    public List<RagSearchResultVO> searchHybrid(String query, float[] embedding, int topK, int hybridTopK, int rrfC) {
        if (!indexExists()) return List.of();
        List<SearchHit> bm25 = bm25Search(query, hybridTopK);
        List<SearchHit> knn = knnSearch(embedding, hybridTopK);
        return rrfFuse(bm25, knn, rrfC).stream().sorted(Comparator.comparingDouble(SearchHit::score).reversed())
                .limit(topK).map(this::toVO).toList();
    }

    private List<SearchHit> bm25Search(String query, int size) {
        try {
            SearchResponse<KbChunkDoc> response = client.search(request -> request.index(properties.getIndex().getName()).size(size)
                    .query(q -> q.multiMatch(match -> match.query(query).fields("content", "documentTitle"))), KbChunkDoc.class);
            return hits(response, false);
        } catch (IOException exception) { throw new IllegalStateException("BM25 search failed", exception); }
    }

    private List<SearchHit> knnSearch(float[] embedding, int size) {
        if (embedding == null || embedding.length == 0) return List.of();
        try {
            SearchResponse<KbChunkDoc> response = client.search(request -> request.index(properties.getIndex().getName()).size(size)
                    .knn(knn -> knn.field("embedding").queryVector(toFloatList(embedding)).k(size).numCandidates(Math.max(size * 2, 10))), KbChunkDoc.class);
            return hits(response, true);
        } catch (IOException exception) { throw new IllegalStateException("kNN search failed", exception); }
    }

    private List<SearchHit> hits(SearchResponse<KbChunkDoc> response, boolean vector) {
        return response.hits().hits().stream().filter(hit -> hit.source() != null).map(hit -> {
            KbChunkDoc source = hit.source();
            return new SearchHit(source, hit.score() == null ? 0 : hit.score(), vector ? 0 : hit.score(), vector ? hit.score() : 0);
        }).toList();
    }

    private List<SearchHit> rrfFuse(List<SearchHit> bm25, List<SearchHit> knn, int constant) {
        Map<String, SearchHit> merged = new HashMap<>();
        fuse(merged, bm25, constant); fuse(merged, knn, constant);
        return new ArrayList<>(merged.values());
    }

    private void fuse(Map<String, SearchHit> merged, List<SearchHit> values, int constant) {
        for (int i = 0; i < values.size(); i++) {
            SearchHit value = values.get(i); String id = value.document().getChunkId();
            SearchHit target = merged.computeIfAbsent(id, ignored -> new SearchHit(value.document(), 0, value.bm25Score(), value.vectorScore()));
            target.setScore(target.score() + 1.0 / (Math.max(1, constant) + i + 1));
            target.setBm25Score(Math.max(target.bm25Score(), value.bm25Score()));
            target.setVectorScore(Math.max(target.vectorScore(), value.vectorScore()));
        }
    }

    private RagSearchResultVO toVO(SearchHit hit) {
        KbChunkDoc document = hit.document();
        return RagSearchResultVO.builder().chunkId(document.getChunkId()).documentId(document.getDocumentId())
                .documentTitle(document.getDocumentTitle()).content(document.getContent()).heading(document.getHeading())
                .score(hit.score()).bm25Score(hit.bm25Score()).vectorScore(hit.vectorScore()).build();
    }

    private List<Float> toFloatList(float[] vector) { List<Float> values = new ArrayList<>(vector.length); for (float value : vector) values.add(value); return values; }
    @Override public void dropIndex() { try { if (indexExists()) client.indices().delete(request -> request.index(properties.getIndex().getName())); } catch (IOException exception) { throw new IllegalStateException("Cannot drop RAG index", exception); } }

    private static final class SearchHit {
        private final KbChunkDoc document; private double score; private double bm25Score; private double vectorScore;
        private SearchHit(KbChunkDoc document, double score, double bm25Score, double vectorScore) { this.document = document; this.score = score; this.bm25Score = bm25Score; this.vectorScore = vectorScore; }
        KbChunkDoc document() { return document; } double score() { return score; } double bm25Score() { return bm25Score; } double vectorScore() { return vectorScore; }
        void setScore(double score) { this.score = score; } void setBm25Score(double score) { this.bm25Score = score; } void setVectorScore(double score) { this.vectorScore = score; }
    }
}
