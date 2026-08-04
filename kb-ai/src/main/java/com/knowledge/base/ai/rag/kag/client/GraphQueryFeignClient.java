package com.knowledge.base.ai.rag.kag.client;

import com.knowledge.base.ai.rag.kag.dto.GraphContext;
import com.knowledge.base.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "kb-graph-query", url = "${graph.service.url}")
public interface GraphQueryFeignClient {
    @GetMapping("/internal/context")
    Result<GraphContext> retrieve(@RequestParam("query") String query);
}
