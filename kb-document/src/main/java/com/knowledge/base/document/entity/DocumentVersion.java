package com.knowledge.base.document.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_document_version")
@Schema(description = "Document version entity")
public class DocumentVersion {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long documentId;
    private Integer version;
    private String title;
    private String content;
    private String summary;
    private String changeDescription;
    private Long changeSize;
    private Long operatorId;
    private String operatorName;
    private LocalDateTime createdAt;
}
