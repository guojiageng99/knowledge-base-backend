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
@Schema(description = "文档处理结果")
public class DocumentProcessVO implements Serializable {
    private String processType;
    private String originalContent;
    private String processedContent;
    private Boolean success;
    private String message;
    private Integer tokens;
}
