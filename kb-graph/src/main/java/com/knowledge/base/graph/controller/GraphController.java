package com.knowledge.base.graph.controller;

import com.knowledge.base.common.result.Result;
import com.knowledge.base.graph.dto.GraphBuildRequest;
import com.knowledge.base.graph.service.GraphService;
import com.knowledge.base.graph.vo.GraphDataVO;
import com.knowledge.base.graph.vo.GraphEdgeVO;
import com.knowledge.base.graph.vo.GraphNodeVO;
import com.knowledge.base.graph.vo.KagContextVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class GraphController {
    private final GraphService graphService;

    @GetMapping("/data") public Result<GraphDataVO> data(@RequestParam(required = false) String type) { return Result.success(graphService.getGraphData(type)); }
    @GetMapping("/nodes") public Result<List<GraphNodeVO>> nodes(@RequestParam(required = false) String type) { return Result.success(graphService.getNodes(type)); }
    @GetMapping("/edges") public Result<List<GraphEdgeVO>> edges(@RequestParam(required = false) String sourceType, @RequestParam(required = false) String targetType) { return Result.success(graphService.getEdges(sourceType, targetType)); }
    @GetMapping("/search") public Result<GraphDataVO> search(@RequestParam String keyword) { return Result.success(graphService.searchGraph(keyword)); }
    @GetMapping("/path") public Result<GraphDataVO> path(@RequestParam String sourceId, @RequestParam String targetId, @RequestParam(defaultValue = "3") Integer maxDepth) { return Result.success(graphService.analyzePath(sourceId, targetId, maxDepth)); }
    @GetMapping("/internal/context") public Result<KagContextVO> context(@RequestParam String query) { return Result.success(graphService.retrieveKagContext(query)); }
    @PostMapping("/internal/rebuild") public Result<Void> rebuild(@RequestBody GraphBuildRequest request) { graphService.rebuildDocument(request); return Result.success(); }
    @DeleteMapping("/internal/documents/{documentId}") public Result<Void> delete(@PathVariable Long documentId) { graphService.deleteDocument(documentId); return Result.success(); }
}
