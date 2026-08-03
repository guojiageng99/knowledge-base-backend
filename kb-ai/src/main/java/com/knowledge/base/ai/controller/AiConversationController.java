package com.knowledge.base.ai.controller;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.knowledge.base.ai.service.AiConversationService;
import com.knowledge.base.ai.vo.ConversationVO;
import com.knowledge.base.common.result.Result;
import com.knowledge.base.common.utils.UserContextUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestController @RequestMapping("/conversation") @RequiredArgsConstructor
public class AiConversationController {
    private final AiConversationService service;
    @PostMapping public Result<ConversationVO> create(@RequestBody(required=false) Map<String,String> body){ Long id=service.createConversation(body==null?"新对话":body.getOrDefault("title","新对话"),user()); return Result.success(service.getConversation(id,user())); }
    @GetMapping("/list") public Result<IPage<ConversationVO>> list(@RequestParam(defaultValue="1") Long current,@RequestParam(defaultValue="50") Long size){ return Result.success(service.listConversations(user(),current,size)); }
    @GetMapping("/{id}") public Result<ConversationVO> get(@PathVariable Long id){ return Result.success(service.getConversation(id,user())); }
    @DeleteMapping("/{id}") public Result<Boolean> delete(@PathVariable Long id){ return Result.success(service.deleteConversation(id,user())); }
    private Long user(){ Long id=UserContextUtil.getCurrentUserId(); return id==null?1L:id; }
}
