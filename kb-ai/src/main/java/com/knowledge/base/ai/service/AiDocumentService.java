package com.knowledge.base.ai.service;

import com.knowledge.base.ai.dto.DocumentProcessDTO;
import com.knowledge.base.ai.vo.DocumentProcessVO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface AiDocumentService {
    DocumentProcessVO generateSummaryByContent(DocumentProcessDTO dto, Long userId);
    SseEmitter generateSummaryByContentStream(DocumentProcessDTO dto, Long userId);
}
