package com.knowledge.base.ai.rag.client;

import com.knowledge.base.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.Map;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "kb-document-rag", url = "${document.service.url}", path = "/documents")
public interface DocumentFeignClient {
    @GetMapping("/{id}") Result<Map<String, Object>> getDocument(@PathVariable("id") Long id);

    @GetMapping("/page") Result<Map<String, Object>> pageDocuments(@RequestParam("current") Long current,
                                                                       @RequestParam("size") Long size,
                                                                       @RequestParam("status") Integer status);
}
