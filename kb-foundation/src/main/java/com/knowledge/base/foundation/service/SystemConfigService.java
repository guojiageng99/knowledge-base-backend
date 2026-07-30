package com.knowledge.base.foundation.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.knowledge.base.foundation.entity.SystemConfig;

import java.util.List;
import java.util.Map;

public interface SystemConfigService {

    IPage<SystemConfig> pageConfigs(Long current, Long size, String category);

    SystemConfig getConfigByKey(String key);

    boolean createConfig(SystemConfig config);

    boolean updateConfig(String key, SystemConfig config);

    boolean deleteConfig(String key);

    List<SystemConfig> getConfigsByCategory(String category);

    Map<String, String> getPublicConfigs();
}
