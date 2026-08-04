package com.knowledge.base.ai.service.impl;

import com.knowledge.base.ai.config.ModelProvider;
import com.knowledge.base.ai.dto.ChatRequestDTO;
import com.knowledge.base.ai.entity.*;
import com.knowledge.base.ai.mapper.MessageMapper;
import com.knowledge.base.ai.service.*;
import com.knowledge.base.ai.vo.ChatResponseVO;
import com.knowledge.base.ai.rag.service.RagChatService;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.data.message.AiMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

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
        Response<AiMessage> response = modelProvider.getModel(model).generate(UserMessage.from(dto.getContent()));
        return saveResponse(conversationId, dto.getContent(), response.content().text());
    }
    @Override public SseEmitter chatStream(ChatRequestDTO dto, Long userId) {
        RagChatService ragChatService = ragChatServiceProvider.getIfAvailable();
        if (dto.isEnableRag() && ragChatService != null) {
            try { return ragChatService.chatWithContextStream(dto, userId); }
            catch (Exception e) { log.warn("RAG stream failed; falling back to standard chat", e); }
        }
        Long conversationId = resolveConversation(dto, userId); SseEmitter emitter = new SseEmitter(30*60*1000L);
        CompletableFuture.runAsync(() -> { try {
            String content = modelProvider.getModel(dto.getModel()).generate(UserMessage.from(dto.getContent())).content().text();
            ChatResponseVO result = saveResponse(conversationId, dto.getContent(), content);
            emitter.send(SseEmitter.event().name("message").data(content));
            emitter.send(SseEmitter.event().name("done").data(result)); emitter.complete();
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
        int tokens=user.getTokens()+ai.getTokens(); conversationService.updateTokens(id, tokens);
        Conversation c=conversationService.getById(id);
        return ChatResponseVO.builder().conversationId(id).messageId(ai.getId()).content(answer).tokens(tokens).title(c==null?"新对话":c.getTitle()).build();
    }
    private int estimate(String text) { if(text==null) return 0; int cn=text.replaceAll("[^\\u4e00-\\u9fa5]","").length(); return cn+(text.length()-cn)/4; }
}
