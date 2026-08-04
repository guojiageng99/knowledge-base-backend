package com.knowledge.base.ai.service.impl;

import com.knowledge.base.ai.config.ModelProvider;
import com.knowledge.base.ai.dto.DocumentProcessDTO;
import com.knowledge.base.ai.service.AiDocumentService;
import com.knowledge.base.ai.vo.DocumentProcessVO;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.output.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.util.DigestUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiDocumentServiceImpl implements AiDocumentService {
    private static final String SUMMARY_CACHE_PREFIX = "ai:summary:";
    private static final Duration SUMMARY_CACHE_TTL = Duration.ofDays(7);
    private static final int MAX_CONTENT_CHARS = 8000;

    private final ModelProvider modelProvider;
    private final StringRedisTemplate redisTemplate;

    @Override
    public DocumentProcessVO generateSummaryByContent(DocumentProcessDTO dto, Long userId) {
        String content = truncateContent(dto.getContent(), MAX_CONTENT_CHARS);
        String cached = getCachedSummary(content);
        if (cached != null) return result(dto, cached, true, "缓存命中", null);
        int length = summaryLength(dto);
        try {
            Response<AiMessage> response = modelProvider.getDefaultModel().generate(UserMessage.from(buildSummaryPrompt(content, dto.getTitle(), length)));
            String summary = response.content().text().trim();
            cacheSummary(content, summary);
            Integer tokens = response.tokenUsage() == null ? null : response.tokenUsage().totalTokenCount();
            return result(dto, summary, true, "生成成功", tokens);
        } catch (Exception e) {
            log.error("生成文档摘要失败: userId={}, title={}", userId, dto.getTitle(), e);
            return result(dto, null, false, "生成失败: " + e.getMessage(), null);
        }
    }

    @Override
    public SseEmitter generateSummaryByContentStream(DocumentProcessDTO dto, Long userId) {
        SseEmitter emitter = new SseEmitter(5 * 60 * 1000L);
        CompletableFuture.runAsync(() -> {
            String content = truncateContent(dto.getContent(), MAX_CONTENT_CHARS);
            try {
                String summary = getCachedSummary(content);
                String message = "缓存命中";
                Integer tokens = null;
                if (summary == null) {
                    Response<AiMessage> response = modelProvider.getDefaultModel().generate(UserMessage.from(buildSummaryPrompt(content, dto.getTitle(), summaryLength(dto))));
                    summary = response.content().text().trim();
                    tokens = response.tokenUsage() == null ? null : response.tokenUsage().totalTokenCount();
                    cacheSummary(content, summary);
                    message = "生成成功";
                }
                List<String> chunks = chunkBySentence(summary);
                for (int i = 0; i < chunks.size(); i++) {
                    emitter.send(SseEmitter.event().name("message").data(chunks.get(i)));
                    if (i + 1 < chunks.size()) Thread.sleep(35L);
                }
                emitter.send(SseEmitter.event().name("done").data(result(dto, summary, true, message, tokens)));
                emitter.complete();
            } catch (Exception e) {
                log.error("流式生成文档摘要失败: userId={}, title={}", userId, dto.getTitle(), e);
                try { emitter.send(SseEmitter.event().name("error").data("生成失败: " + e.getMessage())); }
                catch (IOException ignored) { log.debug("摘要错误事件发送失败", ignored); }
                emitter.completeWithError(e);
            }
        });
        return emitter;
    }

    private DocumentProcessVO result(DocumentProcessDTO dto, String summary, boolean success, String message, Integer tokens) {
        return DocumentProcessVO.builder().processType("summary").originalContent(dto.getContent()).processedContent(summary).success(success).message(message).tokens(tokens).build();
    }

    private int summaryLength(DocumentProcessDTO dto) {
        Integer length = dto.getProcessParams() == null ? null : dto.getProcessParams().getSummaryLength();
        return length == null || length <= 0 ? 200 : Math.min(length, 1000);
    }

    private String buildSummaryPrompt(String content, String title, int length) {
        StringBuilder prompt = new StringBuilder("请为以下文档生成一个简洁的摘要，要求：\n");
        prompt.append("1. 摘要长度约").append(length).append("字\n");
        prompt.append("2. 涵盖文档的核心内容和主要观点\n");
        prompt.append("3. 语言简洁明了\n4. 不要添加任何额外解释\n");
        if (title != null && !title.isBlank()) prompt.append("\n文档标题：").append(title).append('\n');
        return prompt.append("\n文档内容：\n").append(content).toString();
    }

    private String truncateContent(String content, int maxChars) {
        if (content == null || content.length() <= maxChars) return content;
        int headLength = (int) (maxChars * 0.75);
        int tailLength = maxChars - headLength;
        return content.substring(0, headLength) + "\n\n...\n\n" + content.substring(content.length() - tailLength);
    }

    private List<String> chunkBySentence(String content) {
        List<String> chunks = new ArrayList<>();
        if (content == null || content.isBlank()) return chunks;
        StringBuilder current = new StringBuilder();
        for (int i = 0; i < content.length(); i++) {
            char ch = content.charAt(i);
            current.append(ch);
            if (ch == '。' || ch == '！' || ch == '？' || ch == '!' || ch == '?' || ch == '\n' || i == content.length() - 1) {
                chunks.add(current.toString());
                current.setLength(0);
            }
        }
        if (current.length() > 0) chunks.add(current.toString());
        return chunks;
    }

    private String cacheKey(String content) { return SUMMARY_CACHE_PREFIX + DigestUtils.md5DigestAsHex(content.getBytes(StandardCharsets.UTF_8)); }
    private String getCachedSummary(String content) { try { return redisTemplate.opsForValue().get(cacheKey(content)); } catch (Exception e) { log.warn("读取摘要缓存失败", e); return null; } }
    private void cacheSummary(String content, String summary) { try { redisTemplate.opsForValue().set(cacheKey(content), summary, SUMMARY_CACHE_TTL); } catch (Exception e) { log.warn("写入摘要缓存失败", e); } }
}
