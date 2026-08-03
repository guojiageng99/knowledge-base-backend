package com.knowledge.base.ai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description="AI对话信息")
public class ConversationVO {
    private Long id;
    private String title;
    private String model;
    private Integer tokensUsed;
    private Integer messageCount;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<MessageVO> messages;
}
