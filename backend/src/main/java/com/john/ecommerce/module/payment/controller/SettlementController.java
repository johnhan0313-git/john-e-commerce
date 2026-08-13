package com.john.ecommerce.module.payment.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.john.ecommerce.common.module.ModuleCodes;
import com.john.ecommerce.common.module.RequiresModule;
import com.john.ecommerce.common.result.R;
import com.john.ecommerce.module.payment.dto.SettlementBillVO;
import com.john.ecommerce.module.payment.dto.SettlementOrderVO;
import com.john.ecommerce.module.payment.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/settlement")
@RequiredArgsConstructor
@RequiresModule(ModuleCodes.SETTLE)
@PreAuthorize("hasRole('OPS')")
public class SettlementController {

    private final SettlementService settlementService;

    @GetMapping("/order")
    public R<Page<SettlementOrderVO>> listOrders(@RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "20") int size,
                                                  @RequestParam(required = false) Long shopId,
                                                  @RequestParam(required = false) Long merchantId) {
        return R.ok(settlementService.listOrders(page, size, shopId, merchantId));
    }

    @GetMapping("/bill")
    public R<Page<SettlementBillVO>> listBills(@RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "20") int size,
                                               @RequestParam(required = false) Long shopId,
                                               @RequestParam(required = false) Long merchantId) {
        return R.ok(settlementService.listBills(page, size, shopId, merchantId));
    }

    @PostMapping("/bill/{id}/settle")
    public R<Void> settleBill(@PathVariable Long id) {
        settlementService.settleBill(id);
        return R.ok();
    }
}
