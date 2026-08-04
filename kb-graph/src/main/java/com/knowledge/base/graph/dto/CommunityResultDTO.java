package com.knowledge.base.graph.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CommunityResultDTO {
    private String communityId;
    private List<String> entityNames;
    private Integer size;
}
