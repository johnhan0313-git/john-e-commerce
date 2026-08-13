package com.john.ecommerce.module.fulfillment.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.john.ecommerce.common.module.ModuleCodes;
import com.john.ecommerce.common.module.RequiresModule;
import com.john.ecommerce.common.result.R;
import com.john.ecommerce.module.fulfillment.dto.WarehouseCreateDTO;
import com.john.ecommerce.module.fulfillment.dto.WarehouseStockVO;
import com.john.ecommerce.module.fulfillment.dto.WarehouseVO;
import com.john.ecommerce.module.fulfillment.service.WarehouseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/warehouse")
@RequiredArgsConstructor
@RequiresModule(ModuleCodes.FULFILLMENT)
@PreAuthorize("hasRole('OPS')")
public class WarehouseController {

    private final WarehouseService warehouseService;

    @PostMapping
    public R<WarehouseVO> create(@Valid @RequestBody WarehouseCreateDTO dto) {
        return R.ok(warehouseService.create(dto));
    }

    @GetMapping("/{id}")
    public R<WarehouseVO> getById(@PathVariable Long id) {
        return R.ok(warehouseService.getById(id));
    }

    @GetMapping
    public R<Page<WarehouseVO>> list(@RequestParam(defaultValue = "1") int page,
                                     @RequestParam(defaultValue = "20") int size) {
        return R.ok(warehouseService.list(page, size));
    }

    @GetMapping("/{warehouseId}/stock")
    public R<List<WarehouseStockVO>> stockByWarehouse(@PathVariable Long warehouseId) {
        return R.ok(warehouseService.stockByWarehouse(warehouseId));
    }

    @GetMapping("/stock/sku/{skuId}")
    public R<List<WarehouseStockVO>> stockBySku(@PathVariable Long skuId) {
        return R.ok(warehouseService.stockBySku(skuId));
    }
}
