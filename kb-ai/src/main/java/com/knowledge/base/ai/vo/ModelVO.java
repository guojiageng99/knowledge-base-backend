package com.knowledge.base.ai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description="AI模型信息")
public class ModelVO {
    private String key;
    private String displayName;
    private String description;
    private Boolean isDefault;
}
