package com.knowledge.base.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI写作请求参数")
public class WritingRequestDTO {
    @NotBlank(message = "写作主题不能为空")
    @Schema(description = "写作主题/标题", example = "如何写好一份技术方案")
    private String topic;
    private String requirements;
    private String contentType;
    private String style;
    private String tone;
    private Integer length;
    private String existingContent;
    @NotBlank(message = "操作类型不能为空")
    private String actionType;
    private String templateId;
    private String model;
}
