package com.knowledge.base.document.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AutoSaveHistoryQueryDTO {
    @NotNull(message = "Document ID is required")
    private Long documentId;
    private Long current = 1L;
    private Long size = 20L;
}
