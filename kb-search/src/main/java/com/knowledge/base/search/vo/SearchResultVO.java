package com.knowledge.base.search.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SearchResultVO {
    private Long id;
    private String title;
    private String summary;
    private List<String> highlights;
    private String categoryName;
    private String tags;
    private String creatorName;
    private Long viewCount;
    private Long likeCount;
    private Long commentCount;
    private String publishAt;
    private Double score;
    private Double bm25Score;
    private Double vectorScore;
    private List<ChunkResult> chunks;

    @Data
    @Builder
    public static class ChunkResult {
        private String chunkId;
        private String content;
        private String heading;
        private double score;
        private double bm25Score;
        private double vectorScore;
    }
}
