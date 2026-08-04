package com.knowledge.base.ai.rag.kag.service;

import com.knowledge.base.ai.rag.kag.dto.GraphContext;

public interface KAGRetrievalService {
    GraphContext retrieveGraphContext(String query);
}
