package com.knowledge.base.foundation.controller;

import com.knowledge.base.common.result.Result;
import com.knowledge.base.foundation.dto.SettingsDTO;
import com.knowledge.base.foundation.service.SettingsService;
import com.knowledge.base.foundation.vo.SettingsVO;
import com.knowledge.base.foundation.vo.SystemStatusVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/config/settings")
@RequiredArgsConstructor
@Tag(name = "System settings", description = "Grouped system settings management APIs")
public class SettingsController {

    private final SettingsService settingsService;

    @GetMapping
    @Operation(summary = "Get all settings")
    public Result<SettingsVO> getSettings() {
        return Result.success(settingsService.getSettings());
    }

    @PutMapping
    @Operation(summary = "Update one settings section")
    public Result<Boolean> updateSettings(@Valid @RequestBody SettingsDTO settingsDTO) {
        return Result.success("Settings saved", settingsService.updateSettings(settingsDTO));
    }

    @GetMapping("/status")
    @Operation(summary = "Get system status")
    public Result<SystemStatusVO> getSystemStatus() {
        return Result.success(settingsService.getSystemStatus());
    }

    @PostMapping("/cache/clear")
    @Operation(summary = "Clear system cache")
    public Result<String> clearCache() {
        return Result.success("Cache cleared", settingsService.clearCache());
    }

    @PostMapping("/backup")
    @Operation(summary = "Record a system backup request")
    public Result<String> createBackup() {
        return Result.success("Backup request created", settingsService.createBackup());
    }
}
