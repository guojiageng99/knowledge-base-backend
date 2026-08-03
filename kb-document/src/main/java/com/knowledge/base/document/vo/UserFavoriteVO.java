package com.knowledge.base.document.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Schema(description = "User favorite")
public class UserFavoriteVO implements Serializable {
    private Long id;
    private Long userId;
    private Long documentId;
    private String documentTitle;
    private String documentSummary;
    private Long documentCategoryId;
    private String documentCategoryName;
    private Long documentAuthorId;
    private String documentAuthorName;
    private Integer documentStatus;
    private Long documentViewCount;
    private LocalDateTime favoriteTime;
    private Boolean isFavorited;
}
