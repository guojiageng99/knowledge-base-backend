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
@Schema(description = "AI写作结果响应")
public class WritingResultVO implements Serializable {
    private String content;
    private Integer tokens;
    private Integer wordCount;
    private String model;
}
