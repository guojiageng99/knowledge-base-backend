package com.knowledge.base.document.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.knowledge.base.common.config.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("kb_user_favorite")
public class UserFavorite extends BaseEntity {
    private Long userId;
    private Long documentId;
    private String documentTitle;
    private Long documentCategoryId;
    private LocalDateTime favoriteTime;

    @TableField(exist = false)
    private String documentSummary;
    @TableField(exist = false)
    private String documentCategoryName;
    @TableField(exist = false)
    private String documentAuthorName;
    @TableField(exist = false)
    private Long documentAuthorId;
    @TableField(exist = false)
    private Integer documentStatus;
    @TableField(exist = false)
    private Long documentViewCount;
}
