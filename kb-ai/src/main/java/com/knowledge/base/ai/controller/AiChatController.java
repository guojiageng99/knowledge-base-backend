package com.knowledge.base.ai.controller;
import com.knowledge.base.ai.config.ModelProvider;
import com.knowledge.base.ai.dto.ChatRequestDTO;
import com.knowledge.base.ai.service.AiChatService;
import com.knowledge.base.ai.vo.*;
import com.knowledge.base.common.result.Result;
import com.knowledge.base.common.utils.UserContextUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.util.List;
@RestController @RequestMapping("/chat") @RequiredArgsConstructor
public class AiChatController {
    private final AiChatService service; private final ModelProvider provider;
    @GetMapping("/models") public Result<List<ModelVO>> models(){ return Result.success(provider.getAvailableModels()); }
    @PostMapping public Result<ChatResponseVO> chat(@Valid @RequestBody ChatRequestDTO dto){ return Result.success(service.chat(dto,currentUser())); }
    @PostMapping("/stream") public SseEmitter stream(@Valid @RequestBody ChatRequestDTO dto){ return service.chatStream(dto,currentUser()); }
    private Long currentUser(){ Long id=UserContextUtil.getCurrentUserId(); return id==null?1L:id; }
}
