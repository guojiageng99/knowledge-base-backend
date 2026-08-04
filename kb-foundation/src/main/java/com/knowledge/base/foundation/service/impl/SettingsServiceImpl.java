package com.knowledge.base.foundation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.knowledge.base.common.exception.BusinessException;
import com.knowledge.base.common.utils.SnowflakeIdGenerator;
import com.knowledge.base.foundation.dto.SettingsDTO;
import com.knowledge.base.foundation.entity.SystemConfig;
import com.knowledge.base.foundation.mapper.SystemConfigMapper;
import com.knowledge.base.foundation.service.SettingsService;
import com.knowledge.base.foundation.vo.SettingsVO;
import com.knowledge.base.foundation.vo.SystemStatusVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettingsServiceImpl implements SettingsService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Map<String, ConfigDefinition> DEFINITIONS = new LinkedHashMap<>();
    private static final Map<String, List<String>> SECTION_FIELDS = Map.of(
            "basic", List.of("systemName", "systemDescription", "systemVersion", "defaultLanguage", "timezone", "allowRegistration", "requireApproval", "enableComments", "enableAI", "enableAIWriting", "enableFullTextSearch"),
            "security", List.of("passwordPolicy", "sessionTimeout", "enable2FA", "ipRestriction", "passwordMinLength", "requireSpecialChar", "loginMaxRetry"),
            "storage", List.of("maxFileSize", "allowedFileTypes", "storageEndpoints", "storageBucket"),
            "notification", List.of("emailEnabled", "emailHost", "emailPort", "websocketEnabled", "notificationRetentionDays"),
            "ai", List.of("aiModelName", "embeddingModel", "milvusHost", "milvusPort")
    );

    static {
        define("systemName", "system.name", "string", "智能知识库", "SYSTEM");
        define("systemDescription", "system.description", "string", "企业级智能知识管理平台", "SYSTEM");
        define("systemVersion", "system.version", "string", "v2.4.1", "SYSTEM");
        define("defaultLanguage", "system.language", "string", "zh-CN", "SYSTEM");
        define("timezone", "system.timezone", "string", "Asia/Shanghai", "SYSTEM");
        define("allowRegistration", "user.registration.enabled", "boolean", "true", "SYSTEM");
        define("requireApproval", "system.requireApproval", "boolean", "true", "SYSTEM");
        define("enableComments", "system.enableComments", "boolean", "true", "SYSTEM");
        define("enableAI", "system.enableAI", "boolean", "true", "SYSTEM");
        define("enableAIWriting", "system.enableAIWriting", "boolean", "true", "SYSTEM");
        define("enableFullTextSearch", "system.enableFullTextSearch", "boolean", "true", "SYSTEM");
        define("passwordPolicy", "system.passwordPolicy", "string", "medium", "SECURITY");
        define("sessionTimeout", "auth.session.timeout", "number", "3600", "SECURITY");
        define("enable2FA", "system.enable2FA", "boolean", "false", "SECURITY");
        define("ipRestriction", "system.ipRestriction", "boolean", "false", "SECURITY");
        define("passwordMinLength", "auth.password.min.length", "number", "8", "SECURITY");
        define("requireSpecialChar", "auth.password.require.special", "boolean", "true", "SECURITY");
        define("loginMaxRetry", "auth.login.max.retry", "number", "5", "SECURITY");
        define("maxFileSize", "file.upload.max.size", "number", "104857600", "STORAGE");
        define("allowedFileTypes", "file.upload.allowed.types", "string", "pdf,doc,docx,xlsx,pptx,txt,md,jpg,png,gif", "STORAGE");
        define("storageEndpoints", "rustfs.endpoints", "string", "http://localhost:8200", "STORAGE");
        define("storageBucket", "rustfs.bucket", "string", "knowledge-docs", "STORAGE");
        define("emailEnabled", "email.enabled", "boolean", "true", "NOTIFICATION");
        define("emailHost", "email.host", "string", "smtp.example.com", "NOTIFICATION");
        define("emailPort", "email.port", "number", "587", "NOTIFICATION");
        define("websocketEnabled", "websocket.enabled", "boolean", "true", "NOTIFICATION");
        define("notificationRetentionDays", "notification.retention.days", "number", "90", "NOTIFICATION");
        define("aiModelName", "qwen.model.name", "string", "qwen-max", "AI");
        define("embeddingModel", "qwen.embedding.model", "string", "text-embedding-v3", "AI");
        define("milvusHost", "milvus.host", "string", "localhost", "AI");
        define("milvusPort", "milvus.port", "number", "19530", "AI");
    }

    private final SystemConfigMapper systemConfigMapper;
    private final JdbcTemplate jdbcTemplate;
    private final CacheManager cacheManager;
    private volatile LocalDateTime lastBackupTime;

    @Override
    public SettingsVO getSettings() {
        Map<String, String> values = loadConfigValues();
        return SettingsVO.builder()
                .basic(buildSection("basic", values))
                .security(buildSection("security", values))
                .storage(buildSection("storage", values))
                .notification(buildSection("notification", values))
                .ai(buildSection("ai", values))
                .status(getSystemStatus(values))
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"systemConfig", "publicConfigs"}, allEntries = true)
    public Boolean updateSettings(SettingsDTO settingsDTO) {
        String section = settingsDTO.getSection().trim().toLowerCase(Locale.ROOT);
        List<String> allowedFields = SECTION_FIELDS.get(section);
        if (allowedFields == null) {
            throw new BusinessException("Unsupported settings section: " + settingsDTO.getSection());
        }
        for (Map.Entry<String, Object> entry : settingsDTO.getSettings().entrySet()) {
            if (!allowedFields.contains(entry.getKey())) {
                throw new BusinessException("Setting does not belong to section " + section + ": " + entry.getKey());
            }
            upsertConfig(DEFINITIONS.get(entry.getKey()), entry.getValue());
        }
        return true;
    }

    @Override
    public SystemStatusVO getSystemStatus() {
        return getSystemStatus(loadConfigValues());
    }

    @Override
    public String clearCache() {
        java.util.Collection<String> cacheNames = cacheManager.getCacheNames();
        for (String cacheName : cacheNames) {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) cache.clear();
        }
        return "Cleared " + cacheNames.size() + " cache region(s)";
    }

    @Override
    public String createBackup() {
        lastBackupTime = LocalDateTime.now();
        return "Backup request recorded at " + lastBackupTime.format(TIME_FORMATTER);
    }

    private static void define(String field, String key, String type, String defaultValue, String category) {
        DEFINITIONS.put(field, new ConfigDefinition(key, type, defaultValue, category));
    }

    private Map<String, String> loadConfigValues() {
        Map<String, String> values = new HashMap<>();
        for (SystemConfig config : systemConfigMapper.selectList(new LambdaQueryWrapper<SystemConfig>())) {
            values.put(config.getConfigKey(), config.getConfigValue());
        }
        return values;
    }

    private Map<String, Object> buildSection(String section, Map<String, String> values) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (String field : SECTION_FIELDS.get(section)) {
            ConfigDefinition definition = DEFINITIONS.get(field);
            result.put(field, convertValue(values.getOrDefault(definition.key(), definition.defaultValue()), definition.type()));
        }
        return result;
    }

    private Object convertValue(String rawValue, String type) {
        if ("boolean".equals(type)) return "true".equalsIgnoreCase(rawValue) || "1".equals(rawValue);
        if ("number".equals(type)) {
            try {
                return Long.parseLong(rawValue.trim());
            } catch (NumberFormatException exception) {
                log.warn("Invalid number setting value: {}", rawValue);
            }
        }
        return rawValue;
    }

    private void upsertConfig(ConfigDefinition definition, Object value) {
        SystemConfig config = systemConfigMapper.selectByConfigKey(definition.key());
        String stringValue = value instanceof List<?> list
                ? String.join(",", list.stream().map(Objects::toString).toList())
                : Objects.toString(value, "");
        if (config == null) {
            config = new SystemConfig();
            config.setId(SnowflakeIdGenerator.nextId());
            config.setConfigKey(definition.key());
            config.setDescription("Created by settings management");
            config.setIsPublic(1);
            config.setDeleted(0);
            config.setConfigType(definition.type());
            config.setCategory(definition.category());
            config.setConfigValue(stringValue);
            systemConfigMapper.insert(config);
            return;
        }
        config.setConfigValue(stringValue);
        config.setConfigType(definition.type());
        config.setCategory(definition.category());
        systemConfigMapper.updateById(config);
    }

    private SystemStatusVO getSystemStatus(Map<String, String> values) {
        File workspace = new File(System.getProperty("user.dir"));
        long totalStorage = workspace.getTotalSpace();
        long usedStorage = Math.max(0L, totalStorage - workspace.getUsableSpace());
        return SystemStatusVO.builder()
                .version(values.getOrDefault("system.version", "v2.4.1"))
                .runStatus("running")
                .dbStatus(databaseConnected() ? "connected" : "disconnected")
                .lastBackupTime(lastBackupTime == null ? "Not created" : lastBackupTime.format(TIME_FORMATTER))
                .totalStorage(totalStorage)
                .usedStorage(usedStorage)
                .documentCount(queryCount("SELECT COUNT(*) FROM kb_document.kb_document WHERE deleted = 0"))
                .userCount(queryCount("SELECT COUNT(*) FROM kb_user.kb_user WHERE deleted = 0"))
                .startTime(TIME_FORMATTER.format(java.time.Instant.ofEpochMilli(ManagementFactory.getRuntimeMXBean().getStartTime()).atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()))
                .build();
    }

    private boolean databaseConnected() {
        try {
            return Integer.valueOf(1).equals(jdbcTemplate.queryForObject("SELECT 1", Integer.class));
        } catch (Exception exception) {
            log.warn("Unable to check database connectivity", exception);
            return false;
        }
    }

    private long queryCount(String sql) {
        try {
            Long count = jdbcTemplate.queryForObject(sql, Long.class);
            return count == null ? 0L : count;
        } catch (Exception exception) {
            log.warn("Unable to query system status count", exception);
            return 0L;
        }
    }

    private record ConfigDefinition(String key, String type, String defaultValue, String category) { }
}
