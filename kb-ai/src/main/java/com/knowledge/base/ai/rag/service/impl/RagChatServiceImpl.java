package com.knowledge.base.ai.rag.service.impl;

import com.knowledge.base.ai.config.ModelProvider;
import com.knowledge.base.ai.dto.ChatRequestDTO;
import com.knowledge.base.ai.entity.*;
import com.knowledge.base.ai.mapper.MessageMapper;
import com.knowledge.base.ai.rag.config.RagProperties;
import com.knowledge.base.ai.rag.service.*;
import com.knowledge.base.ai.rag.vo.*;
import com.knowledge.base.ai.service.AiConversationService;
import com.knowledge.base.ai.vo.ChatResponseVO;
import dev.langchain4j.data.message.UserMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j @Service @RequiredArgsConstructor
public class RagChatServiceImpl implements RagChatService {
    private final RagRetrievalService retrievalService; private final RagProperties properties; private final ModelProvider models;
    private final MessageMapper messages; private final AiConversationService conversations;
    @Override public ChatResponseVO chatWithContext(ChatRequestDTO request, Long userId) {
        Long conversationId=conversation(request,userId); List<RagSearchResultVO> context=safeRetrieve(request.getContent());
        String answer=models.getModel(request.getModel()).generate(UserMessage.from(prompt(request.getContent(),context))).content().text();
        return persist(conversationId,request.getContent(),answer,context);
    }
    @Override public SseEmitter chatWithContextStream(ChatRequestDTO request,Long userId){ Long id=conversation(request,userId); SseEmitter emitter=new SseEmitter(30*60*1000L); CompletableFuture.runAsync(()->{try{List<RagSearchResultVO> context=safeRetrieve(request.getContent());String answer=models.getModel(request.getModel()).generate(UserMessage.from(prompt(request.getContent(),context))).content().text();ChatResponseVO result=persist(id,request.getContent(),answer,context);emitter.send(SseEmitter.event().name("message").data(answer));emitter.send(SseEmitter.event().name("done").data(result));emitter.complete();}catch(Exception e){try{emitter.send(SseEmitter.event().name("error").data(e.getMessage()));}catch(IOException ignored){}emitter.complete();}});return emitter; }
    private Long conversation(ChatRequestDTO dto,Long userId){Conversation c=dto.getConversationId()==null?null:conversations.getById(dto.getConversationId());return c==null||!userId.equals(c.getUserId())?conversations.createConversation(dto,userId):c.getId();}
    private List<RagSearchResultVO> safeRetrieve(String q){try{return retrievalService.retrieve(q,properties.getRetrieval().getDefaultTopK(),properties.getRerank().isEnabled());}catch(Exception e){log.warn("RAG retrieval unavailable; use LLM only: {}",e.getMessage());return List.of();}}
    private String prompt(String query,List<RagSearchResultVO> chunks){if(chunks.isEmpty())return query;StringBuilder b=new StringBuilder("You are a knowledge-base assistant. Answer only from the reference material. If it is insufficient, say so. Cite sources as [1], [2].\n\nREFERENCE:\n");for(int i=0;i<chunks.size();i++){RagSearchResultVO c=chunks.get(i);b.append('[').append(i+1).append("] ").append(c.getDocumentTitle()).append('\n').append(c.getContent()).append("\n\n");}return b.append("QUESTION:\n").append(query).toString();}
    private ChatResponseVO persist(Long id,String question,String answer,List<RagSearchResultVO> chunks){Message user=Message.builder().conversationId(id).role("user").content(question).tokens(tokens(question)).build();messages.insert(user);Message ai=Message.builder().conversationId(id).role("assistant").content(answer).tokens(tokens(answer)).build();messages.insert(ai);int used=user.getTokens()+ai.getTokens();conversations.updateTokens(id,used);Conversation c=conversations.getById(id);return ChatResponseVO.builder().conversationId(id).messageId(ai.getId()).content(answer).tokens(used).title(c.getTitle()).fromKnowledgeBase(!chunks.isEmpty()).citations(citations(chunks)).build();}
    private List<CitationVO> citations(List<RagSearchResultVO> chunks){return java.util.stream.IntStream.range(0,chunks.size()).mapToObj(i->{RagSearchResultVO c=chunks.get(i);String text=c.getContent();return CitationVO.builder().index(i+1).documentId(c.getDocumentId()).documentTitle(c.getDocumentTitle()).excerpt(text.substring(0,Math.min(160,text.length()))).relevanceScore(c.getScore()).build();}).toList();}
    private int tokens(String value){if(value==null)return 0;int cn=value.replaceAll("[^\\u4e00-\\u9fa5]","").length();return cn+(value.length()-cn)/4;}
}
