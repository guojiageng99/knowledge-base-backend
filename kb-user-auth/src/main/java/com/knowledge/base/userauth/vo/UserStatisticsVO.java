package com.knowledge.base.userauth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Current user statistics")
public class UserStatisticsVO implements Serializable {
    private Long documentCount;
    private Long likeCount;
    private Long viewCount;
    private Long commentCount;
}
