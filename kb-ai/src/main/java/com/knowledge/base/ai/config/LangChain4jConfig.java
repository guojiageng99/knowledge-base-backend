package com.knowledge.base.ai.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.Duration;

@Configuration
public class LangChain4jConfig {
    @Value("${qwen.api-key}") private String qwenApiKey;
    @Value("${qwen.base-url}") private String qwenBaseUrl;
    @Value("${qwen.chat.options.model}") private String qwenModel;
    @Value("${qwen.chat.options.temperature}") private double qwenTemperature;
    @Value("${qwen.chat.options.max-tokens}") private int qwenMaxTokens;
    @Value("${deepseek.api-key}") private String deepseekApiKey;
    @Value("${deepseek.base-url}") private String deepseekBaseUrl;
    @Value("${deepseek.model}") private String deepseekModel;
    @Value("${deepseek.max-tokens}") private int deepseekMaxTokens;
    @Value("${deepseek.temperature}") private double deepseekTemperature;

    @Bean("qwenChatModel")
    public ChatLanguageModel qwenChatModel() {
        return OpenAiChatModel.builder().apiKey(qwenApiKey).baseUrl(qwenBaseUrl).modelName(qwenModel)
                .temperature(qwenTemperature).maxTokens(qwenMaxTokens).timeout(Duration.ofSeconds(120)).build();
    }

    @Bean("deepseekChatModel")
    public ChatLanguageModel deepseekChatModel() {
        return OpenAiChatModel.builder().apiKey(deepseekApiKey).baseUrl(deepseekBaseUrl).modelName(deepseekModel)
                .temperature(deepseekTemperature).maxTokens(deepseekMaxTokens).timeout(Duration.ofSeconds(120)).build();
    }
}
