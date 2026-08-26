package com.john.ecommerce.module.payment.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.john.ecommerce.common.module.ModuleCodes;
import com.john.ecommerce.common.module.RequiresModule;
import com.john.ecommerce.common.result.R;
import com.john.ecommerce.module.payment.dto.PayRouteRuleCreateDTO;
import com.john.ecommerce.module.payment.dto.PayRouteRuleVO;
import com.john.ecommerce.module.payment.service.PayRouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pay-route")
@RequiredArgsConstructor
@RequiresModule(ModuleCodes.PAYMENT)
@PreAuthorize("hasRole('OPS')")
public class PayRouteController {

    private final PayRouteService payRouteService;

    @GetMapping
    public R<Page<PayRouteRuleVO>> list(@RequestParam(defaultValue = "1") int page,
                                        @RequestParam(defaultValue = "20") int size) {
        return R.ok(payRouteService.list(page, size));
    }

    @GetMapping("/{id}")
    public R<PayRouteRuleVO> getById(@PathVariable Long id) {
        return R.ok(payRouteService.getById(id));
    }

    @PostMapping
    public R<PayRouteRuleVO> create(@RequestBody PayRouteRuleCreateDTO dto) {
        return R.ok(payRouteService.create(dto));
    }

    @PutMapping("/{id}")
    public R<PayRouteRuleVO> update(@PathVariable Long id, @RequestBody PayRouteRuleCreateDTO dto) {
        return R.ok(payRouteService.update(id, dto));
    }
}
