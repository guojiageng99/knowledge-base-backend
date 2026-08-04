package com.knowledge.base.search.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SearchHistoryVO {
    private Long id;
    private String keyword;
    private Integer searchCount;
    private LocalDateTime createTime;
}
