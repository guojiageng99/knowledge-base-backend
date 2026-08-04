package com.knowledge.base.foundation.service;

import com.knowledge.base.foundation.dto.SettingsDTO;
import com.knowledge.base.foundation.vo.SettingsVO;
import com.knowledge.base.foundation.vo.SystemStatusVO;

public interface SettingsService {
    SettingsVO getSettings();
    Boolean updateSettings(SettingsDTO settingsDTO);
    SystemStatusVO getSystemStatus();
    String clearCache();
    String createBackup();
}
