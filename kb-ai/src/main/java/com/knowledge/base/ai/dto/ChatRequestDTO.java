package com.knowledge.base.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data @Schema(description="AI对话请求")
public class ChatRequestDTO {
    @NotBlank(message="消息内容不能为空") private String content;
    private Long conversationId;
    private String model;
    private String systemPrompt;
}
