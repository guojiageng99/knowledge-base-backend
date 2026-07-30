package com.knowledge.base.common.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "Pagination query parameters")
public class PageParam implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Current page", example = "1")
    private Long current = 1L;

    @Schema(description = "Page size", example = "10")
    private Long size = 10L;

    @Schema(description = "Sort field")
    private String sortField;

    @Schema(description = "Sort order: asc or desc")
    private String sortOrder = "asc";

    public long getOffset() {
        return (Math.max(current, 1L) - 1) * Math.max(size, 1L);
    }
}
