package com.knowledge.base.document.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文档审核参数")
public class DocumentReviewDTO {

    @NotNull(message = "审核记录ID不能为空")
    @Schema(description = "审核记录ID")
    private Long reviewId;

    @NotNull(message = "审核结果不能为空")
    @Schema(description = "审核结果：1-通过，2-驳回")
    private Integer reviewResult;

    @Schema(description = "审核意见")
    private String reviewComment;
}
