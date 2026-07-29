package com.john.ecommerce.common.module;

public interface TenantModuleChecker {
    boolean isEnabled(Long tenantId, String moduleCode);
}
