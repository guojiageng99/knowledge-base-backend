package com.knowledge.base.file.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "File upload request")
public class FileUploadDTO {

    private Long uploaderId;
    private Integer accessLevel = 0;
    private String storageType = "rustfs";
    private String businessType;
    private Long businessId;
}
