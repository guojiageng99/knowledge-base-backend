package com.knowledge.base.ai.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.Duration;

@Configuration
public class LangChain4jConfig {
    @Value("${openai.api-key}") private String openAiApiKey;
    @Value("${openai.base-url}") private String openAiBaseUrl;
    @Value("${openai.chat.options.model}") private String openAiModel;
    @Value("${openai.chat.options.temperature}") private double openAiTemperature;
    @Value("${openai.chat.options.max-tokens}") private int openAiMaxTokens;

    @Bean("openAiChatModel")
    public ChatLanguageModel openAiChatModel() {
        return OpenAiChatModel.builder().apiKey(openAiApiKey).baseUrl(openAiBaseUrl).modelName(openAiModel)
                .temperature(openAiTemperature).maxTokens(openAiMaxTokens).timeout(Duration.ofSeconds(120)).build();
    }
}
