package com.knowledge.base.document.feign;

import com.knowledge.base.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "kb-search-index", url = "${search.service.url}")
public interface SearchFeignClient {
    @PostMapping("/internal/index/document") Result<Void> indexDocument(@RequestBody Map<String, Object> data);
    @DeleteMapping("/internal/index/document/{documentId}") Result<Void> deleteDocument(@PathVariable("documentId") Long documentId);
}
