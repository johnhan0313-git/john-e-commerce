package com.john.ecommerce.common.context;

public class UserContext {
    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> USER_TYPE = new ThreadLocal<>();

    public static void setUserId(Long id) {
        USER_ID.set(id);
    }

    public static Long getCurrentUserId() {
        return USER_ID.get();
    }

    public static void setUserType(String type) {
        USER_TYPE.set(type);
    }

    public static String getUserType() {
        return USER_TYPE.get();
    }

    public static void clear() {
        USER_ID.remove();
        USER_TYPE.remove();
    }
}
