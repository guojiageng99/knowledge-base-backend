package com.knowledge.base.ai.rag.kag.client;

import com.knowledge.base.ai.rag.kag.dto.GraphBuildRequest;
import com.knowledge.base.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "kb-graph-kag", url = "${graph.service.url}")
public interface GraphFeignClient {
    @PostMapping("/internal/rebuild")
    Result<Void> rebuildDocument(@RequestBody GraphBuildRequest request);

    @DeleteMapping("/internal/documents/{documentId}")
    Result<Void> deleteDocument(@PathVariable("documentId") Long documentId);
}
