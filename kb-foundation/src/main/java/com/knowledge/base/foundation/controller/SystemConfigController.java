package com.knowledge.base.foundation.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.knowledge.base.common.result.Result;
import com.knowledge.base.foundation.entity.SystemConfig;
import com.knowledge.base.foundation.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/config")
@RequiredArgsConstructor
public class SystemConfigController {

    private final SystemConfigService systemConfigService;

    @GetMapping
    public Result<IPage<SystemConfig>> pageConfigs(@RequestParam(defaultValue = "1") Long current,
                                                    @RequestParam(defaultValue = "10") Long size,
                                                    @RequestParam(required = false) String category) {
        return Result.success(systemConfigService.pageConfigs(current, size, category));
    }

    @GetMapping("/public")
    public Result<Map<String, String>> getPublicConfigs() {
        return Result.success(systemConfigService.getPublicConfigs());
    }

    @GetMapping("/category/{category}")
    public Result<List<SystemConfig>> getConfigsByCategory(@PathVariable String category) {
        return Result.success(systemConfigService.getConfigsByCategory(category));
    }

    @GetMapping("/{key}")
    public Result<SystemConfig> getConfigByKey(@PathVariable String key) {
        return Result.success(systemConfigService.getConfigByKey(key));
    }

    @PostMapping
    public Result<Boolean> createConfig(@RequestBody SystemConfig config) {
        return Result.success(systemConfigService.createConfig(config));
    }

    @PutMapping("/{key}")
    public Result<Boolean> updateConfig(@PathVariable String key, @RequestBody SystemConfig config) {
        return Result.success(systemConfigService.updateConfig(key, config));
    }

    @DeleteMapping("/{key}")
    public Result<Boolean> deleteConfig(@PathVariable String key) {
        return Result.success(systemConfigService.deleteConfig(key));
    }
}
