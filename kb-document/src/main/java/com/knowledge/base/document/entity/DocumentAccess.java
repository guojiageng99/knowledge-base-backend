package com.knowledge.base.document.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("kb_document_access")
public class DocumentAccess {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long documentId;
    private String documentTitle;
    private LocalDateTime accessTime;
    @TableField("created_at")
    private LocalDateTime createdAt;
    @TableField("updated_at")
    private LocalDateTime updatedAt;
    @TableField("created_by")
    private Long createdBy;
    @TableField("updated_by")
    private Long updatedBy;
    @TableLogic
    private Integer deleted;

    @TableField(exist = false)
    private String summary;
    @TableField(exist = false)
    private String categoryName;
    @TableField(exist = false)
    private String authorName;
    @TableField(exist = false)
    private Integer status;
}
