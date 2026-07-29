package com.john.ecommerce.module.tenant.controller;

import com.john.ecommerce.common.result.R;
import com.john.ecommerce.module.tenant.dto.ModuleDefVO;
import com.john.ecommerce.module.tenant.service.TenantModuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/module-def")
@RequiredArgsConstructor
public class ModuleDefController {

    private final TenantModuleService tenantModuleService;

    @GetMapping
    public R<List<ModuleDefVO>> list() {
        return R.ok(tenantModuleService.listModuleDefs());
    }
}
