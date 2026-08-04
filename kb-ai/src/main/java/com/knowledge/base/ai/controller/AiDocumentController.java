package com.knowledge.base.ai.controller;

import com.knowledge.base.ai.dto.DocumentProcessDTO;
import com.knowledge.base.ai.service.AiDocumentService;
import com.knowledge.base.ai.vo.DocumentProcessVO;
import com.knowledge.base.common.result.Result;
import com.knowledge.base.common.utils.UserContextUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/document")
@RequiredArgsConstructor
@Tag(name = "AI文档处理", description = "AI文档摘要和处理接口")
public class AiDocumentController {
    private final AiDocumentService aiDocumentService;

    @PostMapping("/summary/content")
    @Operation(summary = "基于内容生成摘要")
    public Result<DocumentProcessVO> generateSummaryByContent(@Valid @RequestBody DocumentProcessDTO dto) {
        return Result.success(aiDocumentService.generateSummaryByContent(dto, currentUser()));
    }

    @PostMapping("/summary/content/stream")
    @Operation(summary = "基于内容流式生成摘要")
    public SseEmitter generateSummaryByContentStream(@Valid @RequestBody DocumentProcessDTO dto) {
        return aiDocumentService.generateSummaryByContentStream(dto, currentUser());
    }

    private Long currentUser() { Long id = UserContextUtil.getCurrentUserId(); return id == null ? 1L : id; }
}
