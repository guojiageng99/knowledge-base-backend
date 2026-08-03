package com.knowledge.base.ai.service;
import com.knowledge.base.ai.dto.ChatRequestDTO;
import com.knowledge.base.ai.vo.ChatResponseVO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
public interface AiChatService {
    ChatResponseVO chat(ChatRequestDTO requestDTO, Long userId);
    SseEmitter chatStream(ChatRequestDTO requestDTO, Long userId);
}
