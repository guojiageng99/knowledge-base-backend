package com.knowledge.base.ai.config;

import com.knowledge.base.ai.vo.ModelVO;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ModelProvider {
    private final Map<String, ChatLanguageModel> models = new ConcurrentHashMap<>();
    private final Map<String, StreamingChatLanguageModel> streamingModels = new ConcurrentHashMap<>();
    @Value("${openai.api-key:}") private String openAiApiKey;
    @Value("${openai.base-url:}") private String openAiBaseUrl;
    @Value("${openai.chat.options.model:gpt-5.4}") private String openAiModel;
    @Value("${openai.chat.options.temperature:0.7}") private double openAiTemperature;
    @Value("${openai.chat.options.max-tokens:2048}") private int openAiMaxTokens;
    @Value("${ai.default-model:openai}") private String defaultModelName;

    public ModelProvider(@Qualifier("openAiChatModel") ChatLanguageModel openAi) {
        models.put("openai", openAi);
    }
    public ChatLanguageModel getModel(String name) { return models.getOrDefault(name, getDefaultModel()); }
    public StreamingChatLanguageModel getStreamingModel(String name) {
        String key = models.containsKey(name) ? name : defaultModelName;
        return streamingModels.computeIfAbsent(key, this::buildStreamingModel);
    }
    public ChatLanguageModel getDefaultModel() { return models.getOrDefault(defaultModelName, models.values().iterator().next()); }
    public String getDefaultModelName() { return defaultModelName; }
    private StreamingChatLanguageModel buildStreamingModel(String name) {
        return switch (name) {
            case "openai" -> OpenAiStreamingChatModel.builder().apiKey(openAiApiKey).baseUrl(openAiBaseUrl).modelName(openAiModel).temperature(openAiTemperature).maxTokens(openAiMaxTokens).build();
            default -> throw new IllegalArgumentException("Unsupported AI model: " + name);
        };
    }
    public List<ModelVO> getAvailableModels() {
        return List.of(ModelVO.builder().key("openai").displayName(openAiModel)
                .description("OpenAI 兼容接口模型").isDefault("openai".equals(defaultModelName)).build());
    }
}
