package com.knowledge.base.ai.service;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.knowledge.base.ai.dto.ChatRequestDTO;
import com.knowledge.base.ai.entity.Conversation;
import com.knowledge.base.ai.vo.ConversationVO;
public interface AiConversationService {
    Long createConversation(ChatRequestDTO dto, Long userId);
    Long createConversation(String title, Long userId);
    ConversationVO getConversation(Long id, Long userId);
    IPage<ConversationVO> listConversations(Long userId, Long current, Long size);
    boolean deleteConversation(Long id, Long userId);
    boolean updateTokens(Long id, Integer tokens);
    boolean updateTitle(Long id, String title);
    boolean incrementMessageCount(Long id, Integer count);
    Conversation getById(Long id);
}
