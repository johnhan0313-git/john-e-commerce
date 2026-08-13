package com.john.ecommerce.module.tenant.controller;

import com.john.ecommerce.common.context.TenantContext;
import com.john.ecommerce.common.result.R;
import com.john.ecommerce.module.tenant.dto.TenantModuleEnableDTO;
import com.john.ecommerce.module.tenant.dto.TenantModuleVO;
import com.john.ecommerce.module.tenant.service.TenantModuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tenant/modules")
@RequiredArgsConstructor
public class TenantModuleController {

    private final TenantModuleService tenantModuleService;

    @GetMapping
    @PreAuthorize("hasRole('OPS')")
    public R<List<TenantModuleVO>> list(@RequestParam(required = false) Long tenantId) {
        return R.ok(tenantModuleService.listTenantModules(tenantId != null ? tenantId : TenantContext.getTenantId()));
    }

    @PostMapping
    @PreAuthorize("hasRole('OPS')")
    public R<TenantModuleVO> enable(@RequestParam(required = false) Long tenantId,
                                    @Valid @RequestBody TenantModuleEnableDTO dto) {
        return R.ok(tenantModuleService.enable(tenantId, dto));
    }

    @DeleteMapping("/{moduleCode}")
    @PreAuthorize("hasRole('OPS')")
    public R<Void> disable(@RequestParam(required = false) Long tenantId,
                           @PathVariable String moduleCode) {
        tenantModuleService.disable(tenantId, moduleCode);
        return R.ok();
    }
}
