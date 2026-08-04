package com.knowledge.base.ai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import com.knowledge.base.ai.rag.vo.CitationVO;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description="AI对话响应")
public class ChatResponseVO {
    private Long conversationId;
    private Long messageId;
    private String content;
    private Integer tokens;
    private String title;
    private List<CitationVO> citations;
    private boolean fromKnowledgeBase;
}
