package com.knowledge.base.ai.rag.controller;
import com.knowledge.base.ai.dto.ChatRequestDTO;
import com.knowledge.base.ai.rag.service.RagChatService;
import com.knowledge.base.ai.vo.ChatResponseVO;
import com.knowledge.base.common.result.Result;
import com.knowledge.base.common.utils.UserContextUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
@RestController @RequestMapping("/rag") @RequiredArgsConstructor public class RagChatController {
 private final RagChatService service; @PostMapping("/chat") public Result<ChatResponseVO> chat(@Valid @RequestBody ChatRequestDTO dto){return Result.success(service.chatWithContext(dto,user()));} @PostMapping("/chat/stream") public SseEmitter stream(@Valid @RequestBody ChatRequestDTO dto){return service.chatWithContextStream(dto,user());} private Long user(){Long id=UserContextUtil.getCurrentUserId();return id==null?1L:id;}
}
