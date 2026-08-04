package com.knowledge.base.userauth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Permission request")
public class PermissionDTO {
    private Long id;

    @NotBlank(message = "Permission name is required")
    private String name;

    @NotBlank(message = "Permission code is required")
    private String code;

    @Schema(description = "menu, button, or api")
    private String type;
    private Long parentId;
    private String menuUrl;
    private String apiUrl;
    private String method;
    private String icon;
    private String description;
    private Integer sortOrder;
    private Integer status;
}
