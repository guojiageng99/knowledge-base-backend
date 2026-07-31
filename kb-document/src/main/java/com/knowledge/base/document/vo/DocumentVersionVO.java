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
@Schema(description = "Document version information")
public class DocumentVersionVO {

    private Long id;
    private Long documentId;
    private Integer version;
    private String title;
    private String changeDescription;
    private Long changeSize;
    private Long operatorId;
    private String operatorName;
    private LocalDateTime createdAt;
}
