package com.knowledge.base.common.util;

import com.knowledge.base.common.exception.BusinessException;
import com.knowledge.base.common.result.ResultCode;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

public final class AssertUtil {

    private AssertUtil() {
    }

    public static void isTrue(boolean expression, String message) {
        if (!expression) throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), message);
    }

    public static void isFalse(boolean expression, String message) {
        if (expression) throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), message);
    }

    public static void isNull(Object object, String message) {
        if (Objects.nonNull(object)) throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), message);
    }

    public static void notNull(Object object, String message) {
        if (Objects.isNull(object)) throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), message);
    }

    public static void hasLength(String text, String message) {
        if (!StringUtils.hasLength(text)) throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), message);
    }

    public static void hasText(String text, String message) {
        if (!StringUtils.hasText(text)) throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), message);
    }

    public static void notEmpty(Collection<?> collection, String message) {
        if (collection == null || collection.isEmpty()) throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), message);
    }

    public static void notEmpty(Map<?, ?> map, String message) {
        if (map == null || map.isEmpty()) throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), message);
    }

    public static void notEmpty(Object[] array, String message) {
        if (array == null || array.length == 0) throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), message);
    }

    public static void state(boolean state, String message) {
        if (!state) throw new BusinessException(message);
    }
}
