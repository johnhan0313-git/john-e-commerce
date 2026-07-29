package com.john.ecommerce.module.payment.controller;

import com.john.ecommerce.common.module.ModuleCodes;
import com.john.ecommerce.common.module.RequiresModule;
import com.john.ecommerce.common.result.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/crossborder")
@RequiredArgsConstructor
@RequiresModule(ModuleCodes.CROSSBORDER)
public class CrossBorderController {

    @PostMapping("/customs")
    public R<Void> declareCustoms() {
        // stub
        return R.ok();
    }

    @GetMapping("/customs/{id}")
    public R<Void> getCustomsStatus(@PathVariable Long id) {
        // stub
        return R.ok();
    }

    @PostMapping("/fx")
    public R<Void> createFxOrder() {
        // stub
        return R.ok();
    }

    @GetMapping("/fx/{id}")
    public R<Void> getFxOrder(@PathVariable Long id) {
        // stub
        return R.ok();
    }
}
