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
    @Value("${qwen.api-key:}") private String qwenApiKey;
    @Value("${qwen.base-url:}") private String qwenBaseUrl;
    @Value("${qwen.chat.options.model:qwen-plus}") private String qwenModel;
    @Value("${qwen.chat.options.temperature:0.7}") private double qwenTemperature;
    @Value("${qwen.chat.options.max-tokens:2048}") private int qwenMaxTokens;
    @Value("${deepseek.api-key:}") private String deepseekApiKey;
    @Value("${deepseek.base-url:}") private String deepseekBaseUrl;
    @Value("${deepseek.model:deepseek-chat}") private String deepseekModel;
    @Value("${deepseek.temperature:0.7}") private double deepseekTemperature;
    @Value("${deepseek.max-tokens:2048}") private int deepseekMaxTokens;
    @Value("${ai.default-model:qwen}") private String defaultModelName;

    public ModelProvider(@Qualifier("qwenChatModel") ChatLanguageModel qwen,
                         @Qualifier("deepseekChatModel") ChatLanguageModel deepseek) {
        models.put("qwen", qwen);
        models.put("deepseek", deepseek);
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
            case "deepseek" -> OpenAiStreamingChatModel.builder().apiKey(deepseekApiKey).baseUrl(deepseekBaseUrl).modelName(deepseekModel).temperature(deepseekTemperature).maxTokens(deepseekMaxTokens).build();
            case "qwen" -> OpenAiStreamingChatModel.builder().apiKey(qwenApiKey).baseUrl(qwenBaseUrl).modelName(qwenModel).temperature(qwenTemperature).maxTokens(qwenMaxTokens).build();
            default -> throw new IllegalArgumentException("Unsupported AI model: " + name);
        };
    }
    public List<ModelVO> getAvailableModels() {
        return List.of(
                ModelVO.builder().key("qwen").displayName("通义千问").description("阿里云大语言模型").isDefault("qwen".equals(defaultModelName)).build(),
                ModelVO.builder().key("deepseek").displayName("DeepSeek").description("擅长代码生成和深度推理").isDefault("deepseek".equals(defaultModelName)).build());
    }
}
