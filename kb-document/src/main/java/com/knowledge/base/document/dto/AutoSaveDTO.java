package com.knowledge.base.document.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Automatic document save request")
public class AutoSaveDTO {
    private Long id;

    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    private String content;

    @Size(max = 500, message = "Summary must not exceed 500 characters")
    private String summary;

    private Long categoryId;
    private Long teamId;
    private String tags;
}
