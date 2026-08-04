package com.knowledge.base.graph.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GraphCommunityVO {
    private String communityId;
    private List<String> entityNames;
    private Integer size;
    private String algorithm;
}
