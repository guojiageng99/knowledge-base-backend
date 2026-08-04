package com.knowledge.base.ai.rag.service;
import com.knowledge.base.ai.rag.entity.DocumentChunk;
import com.knowledge.base.ai.rag.vo.RagSearchResultVO;
import java.util.List;
public interface VectorIndexService { void indexChunks(List<DocumentChunk> chunks); void deleteByDocId(Long documentId); List<RagSearchResultVO> searchHybrid(String query, float[] embedding, int topK, int hybridTopK, int rrfC); boolean indexExists(); void createIndexIfNotExists(); void dropIndex(); }
