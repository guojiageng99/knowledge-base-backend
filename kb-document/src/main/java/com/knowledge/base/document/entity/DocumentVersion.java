package com.knowledge.base.document.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tb_document_version")
@Schema(description = "文档版本实体")
public class DocumentVersion {

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "版本ID")
    private Long id;

    @Schema(description = "文档ID")
    private Long documentId;

    @Schema(description = "版本号")
    private Integer version;

    @Schema(description = "文档标题")
    private String title;

    @Schema(description = "文档内容")
    private String content;

    @Schema(description = "文档摘要")
    private String summary;

    @Schema(description = "版本变更说明")
    private String changeDescription;

    @Schema(description = "变更大小")
    private Long changeSize;

    @Schema(description = "操作人ID")
    private Long operatorId;

    @Schema(description = "操作人姓名")
    private String operatorName;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
