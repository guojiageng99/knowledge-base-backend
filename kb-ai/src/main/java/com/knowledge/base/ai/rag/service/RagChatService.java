package com.knowledge.base.ai.rag.service;
import com.knowledge.base.ai.dto.ChatRequestDTO;
import com.knowledge.base.ai.vo.ChatResponseVO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
public interface RagChatService { ChatResponseVO chatWithContext(ChatRequestDTO request, Long userId); SseEmitter chatWithContextStream(ChatRequestDTO request, Long userId); }
