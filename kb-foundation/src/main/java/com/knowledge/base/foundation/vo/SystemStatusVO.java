package com.knowledge.base.foundation.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Runtime health and capacity summary for the administration settings page. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "System runtime status")
public class SystemStatusVO {
    private String version;
    private String runStatus;
    private String dbStatus;
    private String lastBackupTime;
    private Long totalStorage;
    private Long usedStorage;
    private Long documentCount;
    private Long userCount;
    private String startTime;
}
