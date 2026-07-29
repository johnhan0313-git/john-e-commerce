package com.john.ecommerce.module.fulfillment.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.john.ecommerce.common.module.ModuleCodes;
import com.john.ecommerce.common.module.RequiresModule;
import com.john.ecommerce.common.result.R;
import com.john.ecommerce.module.fulfillment.dto.SupplierCreateDTO;
import com.john.ecommerce.module.fulfillment.dto.SupplierVO;
import com.john.ecommerce.module.fulfillment.service.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/supplier")
@RequiredArgsConstructor
@RequiresModule(ModuleCodes.PURCHASE)
public class SupplierController {

    private final SupplierService supplierService;

    @PostMapping
    public R<SupplierVO> create(@Valid @RequestBody SupplierCreateDTO dto) {
        return R.ok(supplierService.create(dto));
    }

    @GetMapping("/{id}")
    public R<SupplierVO> getById(@PathVariable Long id) {
        return R.ok(supplierService.getById(id));
    }

    @GetMapping
    public R<Page<SupplierVO>> list(@RequestParam(defaultValue = "1") int page,
                                    @RequestParam(defaultValue = "20") int size) {
        return R.ok(supplierService.list(page, size));
    }
}
