package com.knowledge.base.foundation.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/** Complete, sectioned system settings response. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Complete system settings")
public class SettingsVO {
    private Map<String, Object> basic;
    private Map<String, Object> security;
    private Map<String, Object> storage;
    private Map<String, Object> notification;
    private Map<String, Object> ai;
    private SystemStatusVO status;
}
