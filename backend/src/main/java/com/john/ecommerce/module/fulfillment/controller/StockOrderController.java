package com.john.ecommerce.module.fulfillment.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.john.ecommerce.common.module.ModuleCodes;
import com.john.ecommerce.common.module.RequiresModule;
import com.john.ecommerce.common.result.R;
import com.john.ecommerce.module.fulfillment.dto.StockOrderCreateDTO;
import com.john.ecommerce.module.fulfillment.dto.StockOrderVO;
import com.john.ecommerce.module.fulfillment.service.StockOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/stock-order")
@RequiredArgsConstructor
@RequiresModule(ModuleCodes.FULFILLMENT)
@PreAuthorize("hasRole('OPS')")
public class StockOrderController {

    private final StockOrderService stockOrderService;

    @PostMapping
    public R<StockOrderVO> create(@Valid @RequestBody StockOrderCreateDTO dto) {
        return R.ok(stockOrderService.create(dto));
    }

    @GetMapping("/{id}")
    public R<StockOrderVO> getById(@PathVariable Long id) {
        return R.ok(stockOrderService.getById(id));
    }

    @PutMapping("/{id}/confirm")
    public R<StockOrderVO> confirm(@PathVariable Long id) {
        return R.ok(stockOrderService.confirm(id));
    }

    @GetMapping
    public R<Page<StockOrderVO>> list(@RequestParam(defaultValue = "1") int page,
                                      @RequestParam(defaultValue = "20") int size,
                                      @RequestParam(required = false) String orderType) {
        return R.ok(stockOrderService.list(page, size, orderType));
    }
}
