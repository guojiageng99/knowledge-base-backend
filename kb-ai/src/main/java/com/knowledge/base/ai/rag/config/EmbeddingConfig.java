package com.knowledge.base.ai.rag.config;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "rag.enabled", havingValue = "true", matchIfMissing = true)
public class EmbeddingConfig {
    @Bean
    public EmbeddingModel embeddingModel(@Value("${qwen.api-key}") String apiKey,
                                         @Value("${qwen.base-url}") String baseUrl,
                                         RagProperties properties) {
        return OpenAiEmbeddingModel.builder().apiKey(apiKey).baseUrl(baseUrl)
                .modelName(properties.getEmbedding().getModel()).build();
    }
}
