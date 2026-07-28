package com.john.ecommerce.common.context;

public class TenantContext {
    private static final ThreadLocal<Long> TENANT = new ThreadLocal<>();

    public static void setTenantId(Long id) {
        TENANT.set(id);
    }

    public static Long getTenantId() {
        return TENANT.get();
    }

    public static void clear() {
        TENANT.remove();
    }
}
