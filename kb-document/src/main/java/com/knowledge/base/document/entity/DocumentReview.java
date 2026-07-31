package com.knowledge.base.document.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_document_review")
@Schema(description = "文档审核记录实体")
public class DocumentReview {

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "审核记录ID")
    private Long id;

    @Schema(description = "文档ID")
    private Long documentId;

    @Schema(description = "审核人ID")
    private Long reviewerId;

    @Schema(description = "审核人姓名")
    private String reviewerName;

    @Schema(description = "审核结果：1-通过，2-驳回")
    private Integer reviewResult;

    @Schema(description = "审核意见")
    private String reviewComment;

    @Schema(description = "审核前状态")
    private Integer beforeStatus;

    @Schema(description = "审核时间")
    private LocalDateTime reviewedAt;

    @Schema(description = "审核轮次")
    private Integer reviewRound;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
