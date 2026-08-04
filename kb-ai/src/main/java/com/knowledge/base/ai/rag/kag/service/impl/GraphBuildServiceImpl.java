package com.knowledge.base.ai.rag.kag.service.impl;

import com.knowledge.base.ai.rag.client.DocumentFeignClient;
import com.knowledge.base.ai.rag.config.RagProperties;
import com.knowledge.base.ai.rag.entity.DocumentChunk;
import com.knowledge.base.ai.rag.kag.client.GraphFeignClient;
import com.knowledge.base.ai.rag.kag.dto.ChunkEntityMappingDTO;
import com.knowledge.base.ai.rag.kag.dto.ChunkPropsDTO;
import com.knowledge.base.ai.rag.kag.dto.DocumentPropsDTO;
import com.knowledge.base.ai.rag.kag.dto.EntityMergeDTO;
import com.knowledge.base.ai.rag.kag.dto.ExtractionResult;
import com.knowledge.base.ai.rag.kag.dto.GraphBuildRequest;
import com.knowledge.base.ai.rag.kag.service.EntityExtractionService;
import com.knowledge.base.ai.rag.kag.service.GraphBuildService;
import com.knowledge.base.ai.rag.service.ChunkingService;
import com.knowledge.base.common.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GraphBuildServiceImpl implements GraphBuildService {
    private final DocumentFeignClient documentClient;
    private final GraphFeignClient graphClient;
    private final ChunkingService chunkingService;
    private final EntityExtractionService extractionService;
    private final RagProperties ragProperties;

    @Override
    public void buildForDocument(Long documentId) {
        Result<Map<String, Object>> response = documentClient.getDocument(documentId);
        if (response == null || response.getData() == null) throw new IllegalArgumentException("Document not found: " + documentId);
        Map<String, Object> document = response.getData();
        String content = string(document.get("content"));
        String title = string(document.get("title"));
        Long categoryId = number(document.get("categoryId"));
        Long authorId = number(document.get("authorId"));
        Integer status = integer(document.get("status"));
        List<DocumentChunk> chunks = chunkingService.chunk(content, documentId, title, categoryId, authorId, null, status);
        GraphBuildRequest request = new GraphBuildRequest();
        request.setDocument(documentProps(document, documentId));
        request.setChunks(chunks.stream().map(this::chunkProps).toList());
        Map<String, EntityMergeDTO> entities = new LinkedHashMap<>();
        List<com.knowledge.base.ai.rag.kag.dto.RelationMergeDTO> relations = new ArrayList<>();
        List<ChunkEntityMappingDTO> mentions = new ArrayList<>();
        for (DocumentChunk chunk : chunks) {
            ExtractionResult extracted = extractionService.extract(chunk.getContent(), chunk.getHeading());
            for (EntityMergeDTO entity : extracted.getEntities()) {
                entities.putIfAbsent(entity.getName(), entity);
                ChunkEntityMappingDTO mention = new ChunkEntityMappingDTO();
                mention.setChunkId(chunk.getChunkId()); mention.setEntityName(entity.getName()); mention.setConfidence(1.0D);
                mentions.add(mention);
            }
            relations.addAll(extracted.getRelations());
        }
        request.setEntities(new ArrayList<>(entities.values()));
        request.setRelations(relations);
        request.setMentions(mentions);
        graphClient.rebuildDocument(request);
        log.info("Knowledge graph built for document {}, chunks={}, entities={}", documentId, chunks.size(), entities.size());
    }

    @Override
    public void buildBatch(List<Long> documentIds) {
        if (documentIds == null) return;
        for (Long documentId : documentIds) if (documentId != null) buildForDocument(documentId);
    }

    @Override
    public void buildAll() {
        long current = 1;
        long size = Math.max(1, ragProperties.getReindex().getBatchSize());
        while (true) {
            Result<Map<String, Object>> response = documentClient.pageDocuments(current, size, 1);
            if (response == null || response.getData() == null) return;
            Object records = response.getData().get("records");
            if (!(records instanceof List<?> list) || list.isEmpty()) return;
            for (Object record : list) if (record instanceof Map<?, ?> map) {
                Long id = number(map.get("id")); if (id != null) buildForDocument(id);
            }
            Object pages = response.getData().get("pages");
            if (pages instanceof Number pageCount && current >= pageCount.longValue()) return;
            current++;
        }
    }

    @Override
    public void deleteForDocument(Long documentId) {
        if (documentId != null) graphClient.deleteDocument(documentId);
    }

    private DocumentPropsDTO documentProps(Map<String, Object> document, Long documentId) {
        DocumentPropsDTO props = new DocumentPropsDTO();
        props.setDocId(documentId); props.setTitle(string(document.get("title"))); props.setSummary(string(document.get("summary")));
        props.setCategoryId(number(document.get("categoryId"))); props.setAuthorId(number(document.get("authorId"))); props.setAuthorName(string(document.get("authorName")));
        props.setStatus(integer(document.get("status"))); props.setDocumentType(integer(document.get("documentType"))); props.setTags(string(document.get("tags")));
        return props;
    }

    private ChunkPropsDTO chunkProps(DocumentChunk chunk) {
        ChunkPropsDTO props = new ChunkPropsDTO();
        props.setChunkId(chunk.getChunkId()); props.setDocId(chunk.getDocumentId()); props.setContent(chunk.getContent()); props.setHeading(chunk.getHeading());
        props.setChunkIndex(chunk.getChunkIndex()); props.setTotalChunks(chunk.getTotalChunks()); props.setCategoryId(chunk.getCategoryId());
        return props;
    }

    private String string(Object value) { return value == null ? "" : value.toString(); }
    private Long number(Object value) { return value instanceof Number number ? number.longValue() : value == null ? null : Long.valueOf(value.toString()); }
    private Integer integer(Object value) { return value instanceof Number number ? number.intValue() : value == null ? null : Integer.valueOf(value.toString()); }
}
