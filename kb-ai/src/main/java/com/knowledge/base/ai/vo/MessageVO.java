package com.knowledge.base.ai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description="AI消息信息")
public class MessageVO {
    private Long id;
    private Long conversationId;
    private String role;
    private String content;
    private Integer tokens;
    private LocalDateTime createTime;
}
