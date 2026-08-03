package com.knowledge.base.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FeedbackRequestDTO {
    @NotNull private Long conversationId;
    @NotNull private Long messageId;
    @NotBlank private String feedbackType;
    private String feedbackContent;
    private Integer rating;
}
