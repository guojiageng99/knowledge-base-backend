package com.knowledge.base.search.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@Builder
@Document(indexName = "kb_document", createIndex = false)
public class DocumentIndex {
    @Id private String id;
    @Field(type = FieldType.Text) private String title;
    @Field(type = FieldType.Text) private String summary;
    @Field(type = FieldType.Text) private String content;
    @Field(type = FieldType.Long) private Long categoryId;
    @Field(type = FieldType.Keyword) private String categoryName;
    @Field(type = FieldType.Keyword) private String tags;
    @Field(type = FieldType.Long) private Long creatorId;
    @Field(type = FieldType.Keyword) private String creatorName;
    @Field(type = FieldType.Integer) private Integer docStatus;
    @Field(type = FieldType.Long) private Long viewCount;
    @Field(type = FieldType.Long) private Long likeCount;
    @Field(type = FieldType.Long) private Long commentCount;
    @Field(type = FieldType.Boolean) private Boolean isPublic;
    @Field(type = FieldType.Keyword) private String publishAt;
    @Field(type = FieldType.Keyword) private String createdAt;
    @Field(type = FieldType.Keyword) private String updatedAt;
}
