package com.knowledge.base.document.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class DocumentVO implements Serializable {

    private Long id;
    private String title;
    private String summary;
    private String content;
    private Integer contentLength;
    private Integer documentType;
    private String filePath;
    private Long fileSize;
    private String fileExtension;
    private Long categoryId;
    private String categoryName;
    private String tags;
    private Integer status;
    private Integer isTop;
    private Integer isRecommend;
    private Long viewCount;
    private Long likeCount;
    private Long favoriteCount;
    private Long commentCount;
    private LocalDateTime publishTime;
    private Long authorId;
    private String authorName;
    private AuthorVO author;
    private String coverImage;
    private Integer source;
    private String sourceUrl;
    private Integer allowComment;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
