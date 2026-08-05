package com.knowledge.base.ai.service.impl;

import com.knowledge.base.ai.config.ModelProvider;
import com.knowledge.base.ai.dto.ChatRequestDTO;
import com.knowledge.base.ai.entity.*;
import com.knowledge.base.ai.mapper.MessageMapper;
import com.knowledge.base.ai.service.*;
import com.knowledge.base.ai.vo.ChatResponseVO;
import com.knowledge.base.ai.rag.service.RagChatService;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.data.message.AiMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

@Slf4j @Service @RequiredArgsConstructor
public class AiChatServiceImpl implements AiChatService {
    private final ModelProvider modelProvider;
    private final MessageMapper messageMapper;
    private final AiConversationService conversationService;
    private final ObjectProvider<RagChatService> ragChatServiceProvider;
    @Override public ChatResponseVO chat(ChatRequestDTO dto, Long userId) {
        RagChatService ragChatService = ragChatServiceProvider.getIfAvailable();
        if (dto.isEnableRag() && ragChatService != null) {
            try { return ragChatService.chatWithContext(dto, userId); }
            catch (Exception e) { log.warn("RAG chat failed; falling back to standard chat", e); }
        }
        Long conversationId = resolveConversation(dto, userId);
        String model = dto.getModel() == null ? modelProvider.getDefaultModelName() : dto.getModel();
        List<ChatMessage> history = buildChatHistory(conversationId);
        if (dto.getSystemPrompt() != null && !dto.getSystemPrompt().isBlank()) history.add(SystemMessage.from(dto.getSystemPrompt()));
        history.add(UserMessage.from(dto.getContent()));
        Response<AiMessage> response = modelProvider.getModel(model).generate(history);
        return saveResponse(conversationId, dto.getContent(), response.content().text());
    }
    @Override public SseEmitter chatStream(ChatRequestDTO dto, Long userId) {
        RagChatService ragChatService = ragChatServiceProvider.getIfAvailable();
        if (dto.isEnableRag() && ragChatService != null) {
            try { return ragChatService.chatWithContextStream(dto, userId); }
            catch (Exception e) { log.warn("RAG stream failed; falling back to standard chat", e); }
        }
        Long conversationId = resolveConversation(dto, userId); SseEmitter emitter = new SseEmitter(30*60*1000L);
        final Long finalConversationId = conversationId;
        CompletableFuture.runAsync(() -> { try {
            List<ChatMessage> history = buildChatHistory(finalConversationId);
            if (dto.getSystemPrompt() != null && !dto.getSystemPrompt().isBlank()) history.add(SystemMessage.from(dto.getSystemPrompt()));
            history.add(UserMessage.from(dto.getContent()));
            String modelName = dto.getModel() == null ? modelProvider.getDefaultModelName() : dto.getModel();
            StringBuilder answer = new StringBuilder();
            modelProvider.getStreamingModel(modelName).generate(history, new StreamingResponseHandler<AiMessage>() {
                @Override public void onNext(String token) { try { answer.append(token); emitter.send(SseEmitter.event().name("message").data(token)); } catch (IOException e) { throw new RuntimeException(e); } }
                @Override public void onComplete(Response<AiMessage> response) { try { String content = answer.length() == 0 ? response.content().text() : answer.toString(); ChatResponseVO result = saveResponse(finalConversationId, dto.getContent(), content); emitter.send(SseEmitter.event().name("done").data(result)); emitter.complete(); } catch (Exception e) { onError(e); } }
                @Override public void onError(Throwable error) { log.error("AI streaming chat failed", error); try { emitter.send(SseEmitter.event().name("error").data(error.getMessage())); } catch (IOException ignored) {} emitter.complete(); }
            });
        } catch(Exception e) { log.error("AI流式对话失败", e); try { emitter.send(SseEmitter.event().name("error").data(e.getMessage())); } catch(IOException ignored) {} emitter.complete(); } });
        return emitter;
    }
    private Long resolveConversation(ChatRequestDTO dto, Long userId) {
        Conversation c = dto.getConversationId()==null ? null : conversationService.getById(dto.getConversationId());
        if(c==null || c.getUserId()==null || !c.getUserId().equals(userId)) return conversationService.createConversation(dto, userId);
        return c.getId();
    }
    private ChatResponseVO saveResponse(Long id, String question, String answer) {
        Message user = Message.builder().conversationId(id).role("user").content(question).tokens(estimate(question)).build(); messageMapper.insert(user);
        Message ai = Message.builder().conversationId(id).role("assistant").content(answer).tokens(estimate(answer)).build(); messageMapper.insert(ai);
        int tokens=user.getTokens()+ai.getTokens(); conversationService.updateTokens(id, tokens); conversationService.incrementMessageCount(id, 2);
        Conversation c=conversationService.getById(id);
        return ChatResponseVO.builder().conversationId(id).messageId(ai.getId()).content(answer).tokens(tokens).title(c==null?"新对话":c.getTitle()).build();
    }
    private int estimate(String text) { if(text==null) return 0; int cn=text.replaceAll("[^\\u4e00-\\u9fa5]","").length(); return cn+(text.length()-cn)/4; }
    private List<ChatMessage> buildChatHistory(Long conversationId) {
        List<Message> stored = messageMapper.selectList(new LambdaQueryWrapper<Message>().eq(Message::getConversationId, conversationId).orderByDesc(Message::getCreateTime).last("LIMIT 20"));
        Collections.reverse(stored);
        List<ChatMessage> history = new ArrayList<>(stored.size());
        for (Message message : stored) {
            if ("user".equals(message.getRole())) history.add(UserMessage.from(message.getContent()));
            else if ("assistant".equals(message.getRole())) history.add(AiMessage.from(message.getContent()));
            else if ("system".equals(message.getRole())) history.add(SystemMessage.from(message.getContent()));
        }
        return history;
    }
}
