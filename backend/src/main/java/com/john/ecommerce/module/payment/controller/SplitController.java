package com.john.ecommerce.module.payment.controller;

import com.john.ecommerce.common.module.ModuleCodes;
import com.john.ecommerce.common.module.RequiresModule;
import com.john.ecommerce.common.result.R;
import com.john.ecommerce.module.payment.dto.SplitDetailDTO;
import com.john.ecommerce.module.payment.dto.SplitOrderVO;
import com.john.ecommerce.module.payment.service.SplitService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/split")
@RequiredArgsConstructor
@RequiresModule(ModuleCodes.PAYMENT)
@PreAuthorize("hasRole('OPS')")
public class SplitController {

    private final SplitService splitService;

    @PostMapping
    public R<SplitOrderVO> create(@RequestParam Long paymentId, @RequestBody List<SplitDetailDTO> details) {
        return R.ok(splitService.createSplit(paymentId, details));
    }

    @PostMapping("/{id}/confirm")
    public R<Void> confirm(@PathVariable Long id) {
        splitService.confirmSplit(id);
        return R.ok();
    }

    @GetMapping
    public R<List<SplitOrderVO>> listByPayment(@RequestParam Long paymentId) {
        return R.ok(splitService.listByPayment(paymentId));
    }
}
