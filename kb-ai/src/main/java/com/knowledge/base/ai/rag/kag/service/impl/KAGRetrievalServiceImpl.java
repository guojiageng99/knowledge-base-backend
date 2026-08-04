package com.knowledge.base.ai.rag.kag.service.impl;

import com.knowledge.base.ai.rag.kag.client.GraphQueryFeignClient;
import com.knowledge.base.ai.rag.kag.dto.GraphContext;
import com.knowledge.base.ai.rag.kag.service.KAGRetrievalService;
import com.knowledge.base.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KAGRetrievalServiceImpl implements KAGRetrievalService {
    private final GraphQueryFeignClient graphClient;

    @Override
    public GraphContext retrieveGraphContext(String query) {
        if (query == null || query.isBlank()) return empty();
        Result<GraphContext> response = graphClient.retrieve(query);
        return response == null || response.getData() == null ? empty() : response.getData();
    }

    private GraphContext empty() { GraphContext context = new GraphContext(); context.setEntities(List.of()); context.setPaths(List.of()); context.setChunks(List.of()); context.setHasResults(false); return context; }
}
