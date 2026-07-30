package com.knowledge.base.common.utils;

import cn.hutool.core.util.IdUtil;

/**
 * Provides one shared Snowflake ID generator for application-managed IDs.
 */
public final class SnowflakeIdGenerator {

    private SnowflakeIdGenerator() {
    }

    public static long nextId() {
        return IdUtil.getSnowflakeNextId();
    }
}
