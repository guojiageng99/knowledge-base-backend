package com.knowledge.base.document.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.knowledge.base.common.config.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("kb_document")
public class Document extends BaseEntity {

    private String title;
    private String summary;
    private String content;
    private String contentId;
    private Integer contentLength;
    private Integer documentType;
    private String filePath;
    private Long fileSize;
    private String fileExtension;
    private String mimeType;
    private Long categoryId;
    private Long teamId;
    private String tags;
    private Integer status;
    private Integer isPublic;
    private Integer isTop;
    private Integer isRecommend;
    private Long viewCount;
    private Long likeCount;
    private Long favoriteCount;
    private Long commentCount;
    private LocalDateTime publishTime;
    private Long authorId;
    private String authorName;
    private String coverImage;
    private Integer source;
    private String sourceUrl;
    private Integer allowComment;
    private Integer sort;
    private String remark;
    private Integer autoSaveDismissed;
}
