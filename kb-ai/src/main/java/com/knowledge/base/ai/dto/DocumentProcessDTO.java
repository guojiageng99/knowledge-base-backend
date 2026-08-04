package com.knowledge.base.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文档处理参数")
public class DocumentProcessDTO {
    @NotBlank(message = "文档内容不能为空")
    private String content;
    private String title;
    @NotBlank(message = "处理类型不能为空")
    private String processType;
    @Valid
    private ProcessParams processParams;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProcessParams {
        private Integer summaryLength;
        private Integer outlineLevel;
        private String expansionType;
        private String optimizationTarget;
        private String exampleType;
    }
}
