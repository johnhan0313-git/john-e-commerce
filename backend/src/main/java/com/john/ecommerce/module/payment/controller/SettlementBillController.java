package com.john.ecommerce.module.payment.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.john.ecommerce.common.module.ModuleCodes;
import com.john.ecommerce.common.module.RequiresModule;
import com.john.ecommerce.common.result.R;
import com.john.ecommerce.module.payment.dto.SettlementBillVO;
import com.john.ecommerce.module.payment.dto.SettlementVO;
import com.john.ecommerce.module.payment.service.SettlementBillService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/settlement-bill")
@RequiredArgsConstructor
@RequiresModule(ModuleCodes.SETTLE)
@PreAuthorize("hasRole('OPS')")
public class SettlementBillController {

    private final SettlementBillService settlementBillService;

    @GetMapping
    public R<Page<SettlementBillVO>> list(@RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "20") int size,
                                          @RequestParam(required = false) Long shopId,
                                          @RequestParam(required = false) Long merchantId) {
        return R.ok(settlementBillService.listBills(page, size, shopId, merchantId));
    }

    @PostMapping
    public R<SettlementBillVO> create(@RequestParam Long shopId) {
        return R.ok(settlementBillService.createBill(shopId));
    }

    @PostMapping("/{id}/post")
    public R<Void> postOrder(@PathVariable Long id, @RequestParam Long settlementOrderId) {
        settlementBillService.postToBill(settlementOrderId, id);
        return R.ok();
    }

    @PostMapping("/{id}/settle")
    public R<SettlementVO> settle(@PathVariable Long id) {
        return R.ok(settlementBillService.settle(id));
    }
}
