package com.john.ecommerce.module.fulfillment.controller;

import com.john.ecommerce.common.module.ModuleCodes;
import com.john.ecommerce.common.module.RequiresModule;
import com.john.ecommerce.common.result.R;
import com.john.ecommerce.module.fulfillment.dto.StockTransferCreateDTO;
import com.john.ecommerce.module.fulfillment.dto.StockTransferVO;
import com.john.ecommerce.module.fulfillment.service.StockTransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/stock/transfer")
@RequiredArgsConstructor
@RequiresModule(ModuleCodes.FULFILLMENT)
public class StockTransferController {

    private final StockTransferService stockTransferService;

    @PostMapping
    public R<StockTransferVO> create(@Valid @RequestBody StockTransferCreateDTO dto) {
        return R.ok(stockTransferService.create(dto));
    }

    @GetMapping("/{id}")
    public R<StockTransferVO> getById(@PathVariable Long id) {
        return R.ok(stockTransferService.getById(id));
    }

    @PutMapping("/{id}/confirm")
    public R<StockTransferVO> confirm(@PathVariable Long id) {
        return R.ok(stockTransferService.confirm(id));
    }
}
