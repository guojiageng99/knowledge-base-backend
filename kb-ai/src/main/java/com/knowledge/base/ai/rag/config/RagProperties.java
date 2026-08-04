package com.knowledge.base.ai.rag.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "rag")
public class RagProperties {
    private boolean enabled = true;
    private Chunking chunking = new Chunking();
    private Embedding embedding = new Embedding();
    private Retrieval retrieval = new Retrieval();
    private Rerank rerank = new Rerank();
    private Index index = new Index();
    private Reindex reindex = new Reindex();
    private Rabbit rabbit = new Rabbit();
    @Data public static class Chunking { private int chunkSize = 512; private int chunkOverlap = 64; }
    @Data public static class Embedding { private String model = "text-embedding-v3"; private int dimension = 1024; private long cacheTtlSeconds = 86400; }
    @Data public static class Retrieval { private int defaultTopK = 5; private int hybridTopK = 20; private int rrfC = 60; }
    @Data public static class Rerank { private boolean enabled; private String model = "qwen"; }
    @Data public static class Index { private String name = "kb_chunk"; private int shards = 1; private int replicas = 0; }
    @Data public static class Reindex { private int batchSize = 10; private int maxRetries = 3; }
    @Data public static class Rabbit { private boolean enabled; }
}
