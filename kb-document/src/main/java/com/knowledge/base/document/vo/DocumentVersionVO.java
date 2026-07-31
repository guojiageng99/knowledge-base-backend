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
@Schema(description = "文档版本信息")
public class DocumentVersionVO {

    @Schema(description = "版本ID")
    private Long id;

    @Schema(description = "文档ID")
    private Long documentId;

    @Schema(description = "版本号")
    private Integer version;

    @Schema(description = "文档标题")
    private String title;

    @Schema(description = "版本变更说明")
    private String changeDescription;

    @Schema(description = "变更大小")
    private Long changeSize;

    @Schema(description = "操作人ID")
    private Long operatorId;

    @Schema(description = "操作人姓名")
    private String operatorName;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
