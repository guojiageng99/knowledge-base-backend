package com.knowledge.base.file.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "File query request")
public class FileQueryDTO {

    private String fileType;
    private Long uploaderId;
    private String storageType;
    private String keyword;
    private Long current = 1L;
    private Long size = 10L;
}
