package com.knowledge.base.document.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "Favorite operation request")
public class FavoriteDTO implements Serializable {

    @NotNull(message = "Document ID is required")
    @Schema(description = "Document ID")
    private Long documentId;

    @Schema(description = "Operation: add or remove")
    private String action;
}
