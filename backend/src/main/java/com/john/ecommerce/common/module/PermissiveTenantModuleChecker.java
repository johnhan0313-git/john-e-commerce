package com.john.ecommerce.common.module;

/**
 * 默认全部开通；租户模块表就绪后由 TenantModuleService 替换为 DB 校验。
 * 不再注册为 Spring Bean。
 */
public class PermissiveTenantModuleChecker implements TenantModuleChecker {
    @Override
    public boolean isEnabled(Long tenantId, String moduleCode) {
        return true;
    }
}
