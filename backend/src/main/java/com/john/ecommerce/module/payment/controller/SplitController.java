package com.john.ecommerce.module.payment.controller;

import com.john.ecommerce.common.module.ModuleCodes;
import com.john.ecommerce.common.module.RequiresModule;
import com.john.ecommerce.common.result.R;
import com.john.ecommerce.module.payment.entity.SplitDetail;
import com.john.ecommerce.module.payment.entity.SplitOrder;
import com.john.ecommerce.module.payment.service.SplitService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/split")
@RequiredArgsConstructor
@RequiresModule(ModuleCodes.PAYMENT)
public class SplitController {

    private final SplitService splitService;

    @PostMapping
    public R<SplitOrder> create(@RequestParam Long paymentId, @RequestBody List<SplitDetail> details) {
        return R.ok(splitService.createSplit(paymentId, details));
    }

    @PostMapping("/{id}/confirm")
    public R<Void> confirm(@PathVariable Long id) {
        splitService.confirmSplit(id);
        return R.ok();
    }

    @GetMapping
    public R<List<SplitOrder>> listByPayment(@RequestParam Long paymentId) {
        return R.ok(splitService.listByPayment(paymentId));
    }
}
