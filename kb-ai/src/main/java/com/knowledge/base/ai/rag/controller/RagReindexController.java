package com.knowledge.base.ai.rag.controller;
import com.knowledge.base.ai.rag.dto.ReindexRequestDTO;
import com.knowledge.base.ai.rag.service.ReindexService;
import com.knowledge.base.ai.rag.vo.ReindexProgressVO;
import com.knowledge.base.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/rag/reindex") @RequiredArgsConstructor public class RagReindexController {private final ReindexService service;@PostMapping("/{id}")public Result<String> one(@PathVariable Long id){return Result.success(service.reindexByDocId(id));}@PostMapping("/batch")public Result<String> batch(@RequestBody ReindexRequestDTO dto){return Result.success(service.reindexBatch(dto.getDocumentIds()));}@PostMapping("/all")public Result<String> all(){return Result.success(service.reindexAll());}@DeleteMapping("/{id}")public Result<String> delete(@PathVariable Long id){return Result.success(service.deleteByDocId(id));}@GetMapping("/progress/{id}")public Result<ReindexProgressVO> progress(@PathVariable String id){return Result.success(service.getProgress(id));}}
