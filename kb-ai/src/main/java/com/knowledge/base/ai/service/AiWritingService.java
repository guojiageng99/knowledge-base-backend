package com.knowledge.base.ai.service;

import com.knowledge.base.ai.dto.WritingRequestDTO;
import com.knowledge.base.ai.vo.WritingResultVO;
import com.knowledge.base.ai.vo.WritingTemplateVO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

public interface AiWritingService {
    WritingResultVO generate(WritingRequestDTO dto, Long userId);
    SseEmitter generateStream(WritingRequestDTO dto, Long userId);
    WritingResultVO expand(WritingRequestDTO dto, Long userId);
    WritingResultVO optimize(WritingRequestDTO dto, Long userId);
    WritingResultVO continueWriting(WritingRequestDTO dto, Long userId);
    List<WritingTemplateVO> getTemplates();
}
