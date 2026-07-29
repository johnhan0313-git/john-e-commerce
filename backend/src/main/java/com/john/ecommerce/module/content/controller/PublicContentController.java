package com.john.ecommerce.module.content.controller;

import com.john.ecommerce.common.context.TenantContext;
import com.john.ecommerce.common.result.R;
import com.john.ecommerce.module.content.dto.BannerVO;
import com.john.ecommerce.module.content.dto.NavigationVO;
import com.john.ecommerce.module.content.service.ContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Public (no auth) storefront content endpoints.
 * Matched by SecurityConfig permitAll on /public/**.
 * Requires X-Tenant-Id header or tenantId param to scope data.
 */
@RestController
@RequestMapping("/public/content")
@RequiredArgsConstructor
public class PublicContentController {

    private final ContentService contentService;

    @GetMapping("/banner")
    public R<List<BannerVO>> banners(@RequestParam(required = false) String position,
                                     @RequestHeader(value = "X-Tenant-Id", required = false) Long headerTenantId,
                                     @RequestParam(value = "tenantId", required = false) Long paramTenantId) {
        setTenantIfAbsent(headerTenantId, paramTenantId);
        return R.ok(contentService.listActiveBanners(position));
    }

    @GetMapping("/navigation")
    public R<List<NavigationVO>> navigation(@RequestHeader(value = "X-Tenant-Id", required = false) Long headerTenantId,
                                            @RequestParam(value = "tenantId", required = false) Long paramTenantId) {
        setTenantIfAbsent(headerTenantId, paramTenantId);
        return R.ok(contentService.activeNavigationTree());
    }

    private void setTenantIfAbsent(Long header, Long param) {
        if (TenantContext.getTenantId() == null) {
            Long tid = header != null ? header : param;
            if (tid != null) TenantContext.setTenantId(tid);
        }
    }
}
