package com.john.ecommerce.module.tenant.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.john.ecommerce.common.result.R;
import com.john.ecommerce.module.tenant.dto.TenantBrandingUpdateDTO;
import com.john.ecommerce.module.tenant.dto.TenantBrandingVO;
import com.john.ecommerce.module.tenant.dto.TenantCreateDTO;
import com.john.ecommerce.module.tenant.dto.TenantVO;
import com.john.ecommerce.module.tenant.service.TenantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tenant")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;

    @PostMapping
    @PreAuthorize("hasRole('OPS')")
    public R<TenantVO> create(@Valid @RequestBody TenantCreateDTO dto) {
        return R.ok(tenantService.create(dto));
    }

    @GetMapping("/branding")
    public R<TenantBrandingVO> getBranding() {
        return R.ok(tenantService.getCurrentBranding());
    }

    @PutMapping("/branding")
    @PreAuthorize("hasRole('OPS')")
    public R<TenantBrandingVO> updateBranding(@Valid @RequestBody TenantBrandingUpdateDTO dto) {
        return R.ok(tenantService.updateBranding(dto));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('OPS')")
    public R<TenantVO> getById(@PathVariable Long id) {
        return R.ok(tenantService.getById(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('OPS')")
    public R<Page<TenantVO>> list(@RequestParam(defaultValue = "1") int page,
                                  @RequestParam(defaultValue = "20") int size) {
        return R.ok(tenantService.list(page, size));
    }
}
