package com.knowledge.base.foundation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Map;

/** Request body for updating one settings section. */
@Data
@Schema(description = "System settings update request")
public class SettingsDTO {

    @NotBlank(message = "Settings section is required")
    @Schema(description = "Settings section: basic/security/storage/notification/ai")
    private String section;

    @NotEmpty(message = "Settings must not be empty")
    @Schema(description = "Settings key-value pairs")
    private Map<String, Object> settings;
}
