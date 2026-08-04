package com.knowledge.base.ai.rag.vo;
import lombok.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor public class ReindexProgressVO { private String taskId; private String status; private int total; private int completed; private String error; }
