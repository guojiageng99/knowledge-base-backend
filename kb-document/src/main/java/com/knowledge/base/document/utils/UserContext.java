package com.knowledge.base.document.utils;

public final class UserContext {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> USER_NAME = new ThreadLocal<>();

    private UserContext() {
    }

    public static void setCurrentUser(Long userId, String userName) {
        USER_ID.set(userId);
        USER_NAME.set(userName);
    }

    public static Long getCurrentUserId() {
        return USER_ID.get();
    }

    public static String getCurrentUserName() {
        return USER_NAME.get();
    }

    public static void clear() {
        USER_ID.remove();
        USER_NAME.remove();
    }
}
