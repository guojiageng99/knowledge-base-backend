package com.knowledge.base.foundation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.knowledge.base.common.exception.BusinessException;
import com.knowledge.base.common.utils.SnowflakeIdGenerator;
import com.knowledge.base.foundation.entity.SystemConfig;
import com.knowledge.base.foundation.mapper.SystemConfigMapper;
import com.knowledge.base.foundation.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SystemConfigServiceImpl implements SystemConfigService {

    private final SystemConfigMapper systemConfigMapper;

    @Override
    public IPage<SystemConfig> pageConfigs(Long current, Long size, String category) {
        LambdaQueryWrapper<SystemConfig> query = new LambdaQueryWrapper<SystemConfig>()
                .eq(StringUtils.hasText(category), SystemConfig::getCategory, category)
                .orderByAsc(SystemConfig::getId);
        return systemConfigMapper.selectPage(new Page<>(current, size), query);
    }

    @Override
    @Cacheable(value = "systemConfig", key = "#key")
    public SystemConfig getConfigByKey(String key) {
        requireKey(key);
        return systemConfigMapper.selectByConfigKey(key);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"systemConfig", "publicConfigs"}, allEntries = true)
    public boolean createConfig(SystemConfig config) {
        requireKey(config.getConfigKey());
        if (systemConfigMapper.selectByConfigKey(config.getConfigKey()) != null) {
            throw new BusinessException("Configuration key already exists");
        }
        config.setId(SnowflakeIdGenerator.nextId());
        return systemConfigMapper.insert(config) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"systemConfig", "publicConfigs"}, allEntries = true)
    public boolean updateConfig(String key, SystemConfig config) {
        SystemConfig existing = requireConfig(key);
        existing.setConfigValue(config.getConfigValue());
        existing.setConfigType(config.getConfigType());
        existing.setCategory(config.getCategory());
        existing.setDescription(config.getDescription());
        existing.setIsPublic(config.getIsPublic());
        return systemConfigMapper.updateById(existing) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"systemConfig", "publicConfigs"}, allEntries = true)
    public boolean deleteConfig(String key) {
        return systemConfigMapper.deleteById(requireConfig(key).getId()) > 0;
    }

    @Override
    public List<SystemConfig> getConfigsByCategory(String category) {
        if (!StringUtils.hasText(category)) {
            throw new BusinessException("Configuration category is required");
        }
        return systemConfigMapper.selectByCategory(category);
    }

    @Override
    @Cacheable(value = "publicConfigs")
    public Map<String, String> getPublicConfigs() {
        List<SystemConfig> configs = systemConfigMapper.selectPublicConfigs();
        Map<String, String> result = new LinkedHashMap<>();
        for (SystemConfig config : configs) {
            result.put(config.getConfigKey(), config.getConfigValue());
        }
        return result;
    }

    private SystemConfig requireConfig(String key) {
        requireKey(key);
        SystemConfig config = systemConfigMapper.selectByConfigKey(key);
        if (config == null) {
            throw new BusinessException("Configuration does not exist");
        }
        return config;
    }

    private void requireKey(String key) {
        if (!StringUtils.hasText(key)) {
            throw new BusinessException("Configuration key is required");
        }
    }
}
