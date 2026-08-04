package com.knowledge.base.document.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class BatchReviewDTO {
    @NotEmpty(message = "审核任务ID列表不能为空")
    private List<Long> taskIds;
    @NotBlank(message = "审核结果不能为空")
    private String status;
    private String comment;
}
