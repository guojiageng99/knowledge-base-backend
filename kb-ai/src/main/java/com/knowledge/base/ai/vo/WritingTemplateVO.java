package com.knowledge.base.ai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "写作模板响应")
public class WritingTemplateVO implements Serializable {
    private String id;
    private String name;
    private String description;
    private String category;
    private String prompt;
    private String suggestedContentType;
    private String suggestedStyle;
}
