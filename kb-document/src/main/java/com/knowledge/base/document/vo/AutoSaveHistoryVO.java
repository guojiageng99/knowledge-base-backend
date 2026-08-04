package com.knowledge.base.document.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutoSaveHistoryVO {
    private String id;
    private Long documentId;
    private String title;
    private String contentPreview;
    private String content;
    private Integer contentLength;
    private LocalDateTime savedAt;
}
