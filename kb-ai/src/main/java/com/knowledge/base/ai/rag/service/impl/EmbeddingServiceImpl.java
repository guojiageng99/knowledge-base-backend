package com.knowledge.base.ai.rag.service.impl;
import com.knowledge.base.ai.rag.service.EmbeddingService;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import cn.hutool.crypto.digest.DigestUtil;
import com.alibaba.fastjson2.JSON;
import com.knowledge.base.ai.rag.config.RagProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.concurrent.TimeUnit;
@Service @RequiredArgsConstructor public class EmbeddingServiceImpl implements EmbeddingService {
    private final EmbeddingModel model;
    private final RagProperties properties;
    private final StringRedisTemplate redisTemplate;
    @Override public float[] embed(String text){
        String key = "rag:emb:" + DigestUtil.md5Hex(text == null ? "" : text);
        try {
            String cached = redisTemplate.opsForValue().get(key);
            if (cached != null) return JSON.parseObject(cached, float[].class);
        } catch (DataAccessException ignored) { }
        float[] vector = model.embed(TextSegment.from(text == null ? "" : text)).content().vector();
        try { redisTemplate.opsForValue().set(key, JSON.toJSONString(vector), properties.getEmbedding().getCacheTtlSeconds(), TimeUnit.SECONDS); }
        catch (DataAccessException ignored) { }
        return vector;
    }
    @Override public List<float[]> embedBatch(List<String> texts){ return texts.stream().map(this::embed).toList(); }
}
