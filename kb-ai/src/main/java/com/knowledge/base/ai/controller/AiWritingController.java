package com.knowledge.base.ai.controller;

import com.knowledge.base.ai.dto.WritingRequestDTO;
import com.knowledge.base.ai.service.AiWritingService;
import com.knowledge.base.ai.vo.WritingResultVO;
import com.knowledge.base.ai.vo.WritingTemplateVO;
import com.knowledge.base.common.result.Result;
import com.knowledge.base.common.utils.UserContextUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/writing")
@RequiredArgsConstructor
@Tag(name = "AI写作", description = "AI写作相关接口")
public class AiWritingController {
    private final AiWritingService aiWritingService;

    @PostMapping("/generate")
    @Operation(summary = "生成写作内容")
    public Result<WritingResultVO> generate(@Valid @RequestBody WritingRequestDTO dto) { return Result.success(aiWritingService.generate(dto, currentUser())); }

    @PostMapping("/generate/stream")
    @Operation(summary = "流式生成写作内容")
    public SseEmitter generateStream(@Valid @RequestBody WritingRequestDTO dto) { return aiWritingService.generateStream(dto, currentUser()); }

    @PostMapping("/expand")
    @Operation(summary = "扩写内容")
    public Result<WritingResultVO> expand(@Valid @RequestBody WritingRequestDTO dto) { return Result.success(aiWritingService.expand(dto, currentUser())); }

    @PostMapping("/optimize")
    @Operation(summary = "优化润色")
    public Result<WritingResultVO> optimize(@Valid @RequestBody WritingRequestDTO dto) { return Result.success(aiWritingService.optimize(dto, currentUser())); }

    @PostMapping("/continue")
    @Operation(summary = "续写内容")
    public Result<WritingResultVO> continueWriting(@Valid @RequestBody WritingRequestDTO dto) { return Result.success(aiWritingService.continueWriting(dto, currentUser())); }

    @GetMapping("/templates")
    @Operation(summary = "获取写作模板")
    public Result<List<WritingTemplateVO>> getTemplates() { return Result.success(aiWritingService.getTemplates()); }

    private Long currentUser() { Long id = UserContextUtil.getCurrentUserId(); return id == null ? 1L : id; }
}
