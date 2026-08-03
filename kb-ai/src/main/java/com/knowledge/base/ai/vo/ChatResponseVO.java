package com.knowledge.base.ai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description="AI对话响应")
public class ChatResponseVO {
    private Long conversationId;
    private Long messageId;
    private String content;
    private Integer tokens;
    private String title;
}
