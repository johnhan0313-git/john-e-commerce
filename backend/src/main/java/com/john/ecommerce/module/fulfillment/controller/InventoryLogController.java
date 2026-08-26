package com.john.ecommerce.module.fulfillment.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.john.ecommerce.common.module.ModuleCodes;
import com.john.ecommerce.common.module.RequiresModule;
import com.john.ecommerce.common.result.R;
import com.john.ecommerce.module.fulfillment.dto.InventoryLogVO;
import com.john.ecommerce.module.fulfillment.service.InventoryLogQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inventory/logs")
@RequiredArgsConstructor
@RequiresModule(ModuleCodes.FULFILLMENT)
@PreAuthorize("hasRole('OPS')")
public class InventoryLogController {

    private final InventoryLogQueryService inventoryLogQueryService;

    @GetMapping
    public R<Page<InventoryLogVO>> list(@RequestParam(defaultValue = "1") int page,
                                        @RequestParam(defaultValue = "20") int size,
                                        @RequestParam(required = false) Long warehouseId,
                                        @RequestParam(required = false) Long skuId) {
        return R.ok(inventoryLogQueryService.list(page, size, warehouseId, skuId));
    }
}
