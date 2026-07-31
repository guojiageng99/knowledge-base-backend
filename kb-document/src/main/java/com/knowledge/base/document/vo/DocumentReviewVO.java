package com.knowledge.base.document.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文档审核信息")
public class DocumentReviewVO {

    private Long id;
    private Long documentId;
    private String documentTitle;
    private Long reviewerId;
    private String reviewerName;
    private Integer reviewResult;
    private String reviewComment;
    private Integer beforeStatus;
    private LocalDateTime reviewedAt;
    private Integer reviewRound;
    private LocalDateTime createdAt;
}
