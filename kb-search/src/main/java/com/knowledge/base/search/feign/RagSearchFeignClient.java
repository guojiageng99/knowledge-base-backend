package com.knowledge.base.search.feign;

import com.knowledge.base.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "kb-ai-search", url = "${search.ai-service-url}", path = "/rag")
public interface RagSearchFeignClient {
    @PostMapping("/search") Result<List<RagSearchItemVO>> search(@RequestBody RagSearchRequest request);
}
