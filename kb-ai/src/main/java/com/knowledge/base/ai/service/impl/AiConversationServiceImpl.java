package com.knowledge.base.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.knowledge.base.ai.dto.ChatRequestDTO;
import com.knowledge.base.ai.entity.*;
import com.knowledge.base.ai.mapper.*;
import com.knowledge.base.ai.service.AiConversationService;
import com.knowledge.base.ai.vo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service @RequiredArgsConstructor
public class AiConversationServiceImpl extends ServiceImpl<ConversationMapper, Conversation> implements AiConversationService {
    private final ConversationMapper conversationMapper;
    private final MessageMapper messageMapper;

    @Override @Transactional(rollbackFor=Exception.class)
    public Long createConversation(ChatRequestDTO dto, Long userId) {
        String title = dto.getContent() == null ? "新对话" : dto.getContent();
        return createConversation(title.length() > 30 ? title.substring(0, 30) : title, userId, dto.getModel(), dto.getSystemPrompt());
    }
    @Override @Transactional(rollbackFor=Exception.class)
    public Long createConversation(String title, Long userId) { return createConversation(title, userId, "qwen", null); }
    private Long createConversation(String title, Long userId, String model, String prompt) {
        Conversation c = Conversation.builder().title(title == null || title.isBlank() ? "新对话" : title)
                .userId(userId).model(model == null ? "qwen" : model).systemPrompt(prompt).tokensUsed(0).messageCount(0)
                .status(0).createTime(LocalDateTime.now()).updateTime(LocalDateTime.now()).deleted(0).build();
        if (conversationMapper.insert(c) <= 0) throw new IllegalStateException("创建对话失败");
        return c.getId();
    }
    @Override public ConversationVO getConversation(Long id, Long userId) {
        Conversation c = requireOwned(id, userId);
        ConversationVO vo = toVO(c);
        vo.setMessages(messageMapper.selectList(new LambdaQueryWrapper<Message>().eq(Message::getConversationId, id)
                .orderByAsc(Message::getCreateTime)).stream().map(this::toMessageVO).toList());
        return vo;
    }
    @Override public IPage<ConversationVO> listConversations(Long userId, Long current, Long size) {
        return conversationMapper.selectPage(new Page<>(current, size), new LambdaQueryWrapper<Conversation>()
                .eq(Conversation::getUserId, userId).orderByDesc(Conversation::getUpdateTime)).convert(this::toVO);
    }
    @Override @Transactional(rollbackFor=Exception.class) public boolean deleteConversation(Long id, Long userId) { return conversationMapper.deleteById(requireOwned(id, userId).getId()) > 0; }
    @Override @Transactional(rollbackFor=Exception.class) public boolean updateTokens(Long id, Integer tokens) { Conversation c=require(id); c.setTokensUsed((c.getTokensUsed()==null?0:c.getTokensUsed())+(tokens==null?0:tokens)); c.setMessageCount((c.getMessageCount()==null?0:c.getMessageCount())+1); c.setUpdateTime(LocalDateTime.now()); return conversationMapper.updateById(c)>0; }
    @Override @Transactional(rollbackFor=Exception.class) public boolean updateTitle(Long id, String title) { Conversation c=require(id); c.setTitle(title); c.setUpdateTime(LocalDateTime.now()); return conversationMapper.updateById(c)>0; }
    @Override @Transactional(rollbackFor=Exception.class) public boolean incrementMessageCount(Long id, Integer count) { Conversation c=require(id); c.setMessageCount((c.getMessageCount()==null?0:c.getMessageCount())+(count==null?0:count)); return conversationMapper.updateById(c)>0; }
    @Override public Conversation getById(Long id) { return conversationMapper.selectById(id); }
    private Conversation require(Long id) { Conversation c=conversationMapper.selectById(id); if(c==null) throw new IllegalArgumentException("对话不存在"); return c; }
    private Conversation requireOwned(Long id, Long userId) { Conversation c=require(id); if(c.getUserId()==null || !c.getUserId().equals(userId)) throw new SecurityException("无权访问该对话"); return c; }
    private ConversationVO toVO(Conversation c) { return ConversationVO.builder().id(c.getId()).title(c.getTitle()).model(c.getModel()).tokensUsed(c.getTokensUsed()).messageCount(c.getMessageCount()).status(c.getStatus()).createTime(c.getCreateTime()).updateTime(c.getUpdateTime()).build(); }
    private MessageVO toMessageVO(Message m) { return MessageVO.builder().id(m.getId()).conversationId(m.getConversationId()).role(m.getRole()).content(m.getContent()).tokens(m.getTokens()).createTime(m.getCreateTime()).build(); }
}
