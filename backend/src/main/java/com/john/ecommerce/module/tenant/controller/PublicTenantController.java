package com.john.ecommerce.module.tenant.controller;

import com.john.ecommerce.common.context.TenantContext;
import com.john.ecommerce.common.exception.BizException;
import com.john.ecommerce.common.result.R;
import com.john.ecommerce.module.tenant.dto.TenantBrandingVO;
import com.john.ecommerce.module.tenant.service.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Public storefront branding. SecurityConfig permitAll on /public/**.
 * Requires X-Tenant-Id header or tenantId query.
 */
@RestController
@RequestMapping("/public/tenant")
@RequiredArgsConstructor
public class PublicTenantController {

    private final TenantService tenantService;

    @GetMapping("/branding")
    public R<TenantBrandingVO> branding(
            @RequestHeader(value = "X-Tenant-Id", required = false) Long headerTenantId,
            @RequestParam(value = "tenantId", required = false) Long paramTenantId) {
        resolveTenant(headerTenantId, paramTenantId);
        return R.ok(tenantService.getBranding(TenantContext.getTenantId()));
    }

    private void resolveTenant(Long header, Long param) {
        if (TenantContext.getTenantId() != null) return;
        Long tid = header != null ? header : param;
        if (tid == null) {
            throw new BizException(400, "缺少租户上下文（X-Tenant-Id）");
        }
        TenantContext.setTenantId(tid);
    }
}
