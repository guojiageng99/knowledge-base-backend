package com.knowledge.base.ai.rag.kag.service;

import java.util.List;

public interface GraphBuildService {
    void buildForDocument(Long documentId);
    void buildBatch(List<Long> documentIds);
    void buildAll();
    void deleteForDocument(Long documentId);
}
