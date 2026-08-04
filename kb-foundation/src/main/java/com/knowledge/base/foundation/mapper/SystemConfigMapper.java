package com.knowledge.base.foundation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.knowledge.base.foundation.entity.SystemConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SystemConfigMapper extends BaseMapper<SystemConfig> {

    @Select("SELECT * FROM kb_system_config WHERE config_key = #{configKey} AND deleted = 0")
    SystemConfig selectByConfigKey(@Param("configKey") String configKey);

    @Select("SELECT * FROM kb_system_config WHERE category = #{category} AND deleted = 0 ORDER BY id")
    List<SystemConfig> selectByCategory(@Param("category") String category);

    @Select("SELECT * FROM kb_system_config WHERE is_public = 1 AND deleted = 0 ORDER BY category, config_key")
    List<SystemConfig> selectPublicConfigs();
}
