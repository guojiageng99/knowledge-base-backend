package com.knowledge.base.ai.rag.kag.mq;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class KAGBuildMessage {
    private Type type;
    private List<Long> documentIds;

    public enum Type { BUILD_BY_DOC_IDS, BUILD_ALL, DELETE_BY_DOC_IDS }
}
