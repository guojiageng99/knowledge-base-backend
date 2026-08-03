package com.knowledge.base.document.utils;

import com.knowledge.base.common.utils.UserContextUtil;

public final class UserContext {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> USER_NAME = new ThreadLocal<>();

    private UserContext() {
    }

    public static void setCurrentUser(Long userId, String userName) {
        UserContextUtil.setUserId(userId);
        UserContextUtil.setUsername(userName);
    }

    public static Long getCurrentUserId() {
        Long userId = UserContextUtil.getCurrentUserId();
        return userId != null ? userId : USER_ID.get();
    }

    public static String getCurrentUserName() {
        String username = UserContextUtil.getCurrentUsername();
        return username != null ? username : USER_NAME.get();
    }

    public static void clear() {
        USER_ID.remove();
        USER_NAME.remove();
        UserContextUtil.clear();
    }
}
