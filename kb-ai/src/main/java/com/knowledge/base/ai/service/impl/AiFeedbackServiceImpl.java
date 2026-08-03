package com.knowledge.base.ai.service.impl;
import com.knowledge.base.ai.dto.FeedbackRequestDTO;
import com.knowledge.base.ai.entity.AiFeedback;
import com.knowledge.base.ai.mapper.AiFeedbackMapper;
import com.knowledge.base.ai.service.AiFeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
@Service @RequiredArgsConstructor
public class AiFeedbackServiceImpl implements AiFeedbackService {
    private final AiFeedbackMapper mapper;
    @Override public boolean submit(FeedbackRequestDTO r, Long userId) { return mapper.insert(AiFeedback.builder().conversationId(r.getConversationId()).messageId(r.getMessageId()).userId(userId).feedbackType(r.getFeedbackType()).feedbackContent(r.getFeedbackContent()).rating(r.getRating()).createTime(LocalDateTime.now()).deleted(0).build()) > 0; }
}
