package com.john.ecommerce.module.fulfillment.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.john.ecommerce.common.module.ModuleCodes;
import com.john.ecommerce.common.module.RequiresModule;
import com.john.ecommerce.common.result.R;
import com.john.ecommerce.module.fulfillment.dto.PurchaseOrderCreateDTO;
import com.john.ecommerce.module.fulfillment.dto.PurchaseOrderVO;
import com.john.ecommerce.module.fulfillment.service.PurchaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/purchase")
@RequiredArgsConstructor
@RequiresModule(ModuleCodes.PURCHASE)
public class PurchaseController {

    private final PurchaseService purchaseService;

    @PostMapping
    public R<PurchaseOrderVO> create(@Valid @RequestBody PurchaseOrderCreateDTO dto) {
        return R.ok(purchaseService.create(dto));
    }

    @GetMapping("/{id}")
    public R<PurchaseOrderVO> getById(@PathVariable Long id) {
        return R.ok(purchaseService.getById(id));
    }

    @PutMapping("/{id}/approve")
    public R<PurchaseOrderVO> approve(@PathVariable Long id) {
        return R.ok(purchaseService.approve(id));
    }

    @PutMapping("/{id}/receive")
    public R<PurchaseOrderVO> receive(@PathVariable Long id) {
        return R.ok(purchaseService.receive(id));
    }

    @GetMapping
    public R<Page<PurchaseOrderVO>> list(@RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "20") int size,
                                         @RequestParam(required = false) String status) {
        return R.ok(purchaseService.list(page, size, status));
    }
}
