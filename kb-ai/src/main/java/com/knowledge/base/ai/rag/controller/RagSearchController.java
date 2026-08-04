package com.knowledge.base.ai.rag.controller;
import com.knowledge.base.ai.rag.dto.RagSearchRequestDTO;
import com.knowledge.base.ai.rag.service.RagRetrievalService;
import com.knowledge.base.ai.rag.vo.RagSearchResultVO;
import com.knowledge.base.common.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/rag") @RequiredArgsConstructor public class RagSearchController {private final RagRetrievalService service;@PostMapping("/search")public Result<List<RagSearchResultVO>> search(@Valid @RequestBody RagSearchRequestDTO dto){try{return Result.success(service.retrieve(dto.getQuery(),dto.getTopK()==null?5:dto.getTopK(),dto.isEnableRerank()));}catch(RuntimeException e){return Result.error(503,"RAG检索依赖不可用，请检查 Elasticsearch 和嵌入模型配置");}}}
