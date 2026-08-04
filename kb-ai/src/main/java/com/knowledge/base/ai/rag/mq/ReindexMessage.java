package com.knowledge.base.ai.rag.mq;

import lombok.*;
import java.io.Serializable;
import java.util.List;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ReindexMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private String taskId;
    private ReindexType type;
    private List<Long> documentIds;
    public enum ReindexType { ALL, BY_DOC_IDS, DELETE_BY_DOC_IDS }
}
