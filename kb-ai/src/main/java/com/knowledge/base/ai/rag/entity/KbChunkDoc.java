package com.knowledge.base.ai.rag.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import java.time.LocalDateTime;

@Data
@Document(indexName = "kb_chunk")
public class KbChunkDoc {
    @Id
    private String chunkId;
    @Field(type = FieldType.Long)
    private Long documentId;
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String documentTitle;
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String content;
    @Field(type = FieldType.Keyword)
    private String heading;
    @Field(type = FieldType.Integer)
    private Integer chunkIndex;
    @Field(type = FieldType.Integer)
    private Integer totalChunks;
    @Field(type = FieldType.Long)
    private Long categoryId;
    @Field(type = FieldType.Long)
    private Long authorId;
    @Field(type = FieldType.Long)
    private Long teamId;
    @Field(type = FieldType.Integer)
    private Integer docStatus;
    @Field(type = FieldType.Date)
    private LocalDateTime indexedAt;
    private float[] embedding;
}
