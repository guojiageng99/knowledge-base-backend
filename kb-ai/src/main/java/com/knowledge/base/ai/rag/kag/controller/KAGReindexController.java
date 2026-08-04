package com.knowledge.base.ai.rag.kag.controller;

import com.knowledge.base.ai.rag.kag.mq.KAGBuildDispatcher;
import com.knowledge.base.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/kag")
@RequiredArgsConstructor
public class KAGReindexController {
    private final KAGBuildDispatcher dispatcher;

    @PostMapping("/build/{documentId}") public Result<Void> build(@PathVariable Long documentId) { dispatcher.build(documentId); return Result.success(); }
    @PostMapping("/build/batch") public Result<Void> batch(@RequestBody List<Long> documentIds) { dispatcher.buildBatch(documentIds); return Result.success(); }
    @PostMapping("/build/all") public Result<Void> all() { dispatcher.buildAll(); return Result.success(); }
    @DeleteMapping("/build/{documentId}") public Result<Void> delete(@PathVariable Long documentId) { dispatcher.delete(documentId); return Result.success(); }
}
