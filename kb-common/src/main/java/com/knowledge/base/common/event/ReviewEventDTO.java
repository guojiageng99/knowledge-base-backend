package com.knowledge.base.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/** Event exchanged between document review and foundation notifications. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewEventDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String eventType;
    private Long documentId;
    private String documentTitle;
    private Long authorId;
    private String authorName;
    private Long reviewerId;
    private String reviewerName;
    private Integer reviewRound;
    private Integer reviewLevel;
    private String reviewComment;
    private LocalDateTime timestamp;
}
