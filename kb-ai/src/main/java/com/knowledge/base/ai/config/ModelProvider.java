package com.knowledge.base.ai.config;

import com.knowledge.base.ai.vo.ModelVO;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ModelProvider {
    private final Map<String, ChatLanguageModel> models = new ConcurrentHashMap<>();
    @Value("${ai.default-model:qwen}") private String defaultModelName;

    public ModelProvider(@Qualifier("qwenChatModel") ChatLanguageModel qwen,
                         @Qualifier("deepseekChatModel") ChatLanguageModel deepseek) {
        models.put("qwen", qwen);
        models.put("deepseek", deepseek);
    }
    public ChatLanguageModel getModel(String name) { return models.getOrDefault(name, getDefaultModel()); }
    public ChatLanguageModel getDefaultModel() { return models.getOrDefault(defaultModelName, models.values().iterator().next()); }
    public String getDefaultModelName() { return defaultModelName; }
    public List<ModelVO> getAvailableModels() {
        return List.of(
                ModelVO.builder().key("qwen").displayName("通义千问").description("阿里云大语言模型").isDefault("qwen".equals(defaultModelName)).build(),
                ModelVO.builder().key("deepseek").displayName("DeepSeek").description("擅长代码生成和深度推理").isDefault("deepseek".equals(defaultModelName)).build());
    }
}
