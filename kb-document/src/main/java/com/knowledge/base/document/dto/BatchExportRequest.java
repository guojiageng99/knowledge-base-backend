package com.knowledge.base.document.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchExportRequest {

    /** String IDs preserve Snowflake ID precision across JavaScript and JSON. */
    @NotEmpty(message = "Document ID list must not be empty")
    private List<String> documentIds;

    @NotBlank(message = "Export format must not be empty")
    private String format;
}
