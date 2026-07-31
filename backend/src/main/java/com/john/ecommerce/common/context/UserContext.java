package com.john.ecommerce.common.context;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class UserContext {
    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> USER_TYPE = new ThreadLocal<>();
    private static final ThreadLocal<Set<String>> IDENTITIES = new ThreadLocal<>();

    public static void setUserId(Long id) {
        USER_ID.set(id);
    }

    public static Long getCurrentUserId() {
        return USER_ID.get();
    }

    /** @deprecated 兼容旧 JWT；新逻辑用 {@link #hasIdentity(String)} */
    public static void setUserType(String type) {
        USER_TYPE.set(type);
    }

    /** @deprecated 兼容旧 JWT；新逻辑用 {@link #hasIdentity(String)} */
    public static String getUserType() {
        return USER_TYPE.get();
    }

    public static void setIdentities(Set<String> identities) {
        if (identities == null || identities.isEmpty()) {
            IDENTITIES.set(Collections.emptySet());
        } else {
            IDENTITIES.set(Collections.unmodifiableSet(new LinkedHashSet<>(identities)));
        }
    }

    public static Set<String> getIdentities() {
        Set<String> set = IDENTITIES.get();
        return set != null ? set : Collections.emptySet();
    }

    public static boolean hasIdentity(String identityCode) {
        return identityCode != null && getIdentities().contains(identityCode);
    }

    public static void clear() {
        USER_ID.remove();
        USER_TYPE.remove();
        IDENTITIES.remove();
    }
}
