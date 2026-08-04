package com.knowledge.base.ai.rag.service;
import com.knowledge.base.ai.rag.entity.DocumentChunk;
import java.util.List;
public interface ChunkingService { List<DocumentChunk> chunk(String content, Long documentId, String title, Long categoryId, Long authorId, Long teamId, Integer status); }
