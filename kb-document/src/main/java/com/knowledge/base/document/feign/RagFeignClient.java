package com.knowledge.base.document.feign;

import com.knowledge.base.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "kb-ai-rag", url = "${ai.service.url}", path = "/rag/reindex")
public interface RagFeignClient {
    @PostMapping("/{documentId}")
    Result<String> reindexDocument(@PathVariable("documentId") Long documentId);

    @DeleteMapping("/{documentId}")
    Result<String> removeFromIndex(@PathVariable("documentId") Long documentId);
}
