package com.knowledge.base.document.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("kb_document_tag")
public class DocumentTag {

    private Long id;
    private Long documentId;
    private Long tagId;
    private LocalDateTime createTime;
}
