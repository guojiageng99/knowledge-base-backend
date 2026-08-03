package com.knowledge.base.ai.controller;
import com.knowledge.base.ai.dto.FeedbackRequestDTO;
import com.knowledge.base.ai.service.AiFeedbackService;
import com.knowledge.base.common.result.Result;
import com.knowledge.base.common.utils.UserContextUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/feedback") @RequiredArgsConstructor
public class AiFeedbackController {
    private final AiFeedbackService service;
    @PostMapping public Result<Boolean> submit(@Valid @RequestBody FeedbackRequestDTO dto){ Long id=UserContextUtil.getCurrentUserId(); return Result.success(service.submit(dto,id==null?1L:id)); }
}
