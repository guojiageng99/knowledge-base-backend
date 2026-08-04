package com.knowledge.base.ai.rag.kag.dto;

import lombok.Data;

import java.util.List;

@Data
public class GraphContext {
    private List<GraphEntity> entities;
    private List<GraphPath> paths;
    private List<GraphChunk> chunks;
    private boolean hasResults;

    @Data public static class GraphEntity { private String name; private String type; private String description; private int connectionCount; }
    @Data public static class GraphPath { private List<String> nodes; private List<String> relations; private int hops; }
    @Data public static class GraphChunk { private String chunkId; private Long docId; private String content; private String heading; }
}
