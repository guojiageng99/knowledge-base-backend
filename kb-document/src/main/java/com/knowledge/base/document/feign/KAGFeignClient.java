package com.knowledge.base.document.feign;

import com.knowledge.base.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "kb-ai-kag", url = "${ai.service.url}", path = "/kag")
public interface KAGFeignClient {
    @PostMapping("/build/{documentId}")
    Result<Void> buildGraph(@PathVariable("documentId") Long documentId);

    @DeleteMapping("/build/{documentId}")
    Result<Void> deleteGraph(@PathVariable("documentId") Long documentId);
}
