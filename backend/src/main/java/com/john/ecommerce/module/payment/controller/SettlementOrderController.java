package com.john.ecommerce.module.payment.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.john.ecommerce.common.module.ModuleCodes;
import com.john.ecommerce.common.module.RequiresModule;
import com.john.ecommerce.common.result.R;
import com.john.ecommerce.module.payment.entity.SettlementOrder;
import com.john.ecommerce.module.payment.service.SettlementBillService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/settlement-order")
@RequiredArgsConstructor
@RequiresModule(ModuleCodes.SETTLE)
public class SettlementOrderController {

    private final SettlementBillService settlementBillService;

    @GetMapping
    public R<Page<SettlementOrder>> list(@RequestParam(defaultValue = "1") int page,
                                        @RequestParam(defaultValue = "20") int size,
                                        @RequestParam(required = false) Long shopId,
                                        @RequestParam(required = false) Long merchantId) {
        return R.ok(settlementBillService.listOrders(page, size, shopId, merchantId));
    }
}
