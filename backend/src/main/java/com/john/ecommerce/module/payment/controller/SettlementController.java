package com.john.ecommerce.module.payment.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.john.ecommerce.common.module.ModuleCodes;
import com.john.ecommerce.common.module.RequiresModule;
import com.john.ecommerce.common.result.R;
import com.john.ecommerce.module.payment.dto.SettlementBillVO;
import com.john.ecommerce.module.payment.dto.SettlementOrderVO;
import com.john.ecommerce.module.payment.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/settlement")
@RequiredArgsConstructor
@RequiresModule(ModuleCodes.SETTLE)
public class SettlementController {

    private final SettlementService settlementService;

    @GetMapping("/order")
    public R<Page<SettlementOrderVO>> listOrders(@RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "20") int size) {
        return R.ok(settlementService.listOrders(page, size));
    }

    @GetMapping("/bill")
    public R<Page<SettlementBillVO>> listBills(@RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "20") int size) {
        return R.ok(settlementService.listBills(page, size));
    }

    @PostMapping("/bill/{id}/settle")
    public R<Void> settleBill(@PathVariable Long id) {
        settlementService.settleBill(id);
        return R.ok();
    }
}
