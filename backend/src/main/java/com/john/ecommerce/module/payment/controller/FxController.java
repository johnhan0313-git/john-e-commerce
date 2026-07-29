package com.john.ecommerce.module.payment.controller;

import com.john.ecommerce.common.module.ModuleCodes;
import com.john.ecommerce.common.module.RequiresModule;
import com.john.ecommerce.common.result.R;
import com.john.ecommerce.module.payment.entity.FxOrder;
import com.john.ecommerce.module.payment.service.CrossBorderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/fx")
@RequiredArgsConstructor
@RequiresModule(ModuleCodes.CROSSBORDER)
public class FxController {

    private final CrossBorderService crossBorderService;

    @PostMapping
    public R<FxOrder> create(@RequestBody Map<String, Object> body) {
        Long paymentId = Long.valueOf(body.get("paymentId").toString());
        Long orderId = Long.valueOf(body.get("orderId").toString());
        String sellCurrency = (String) body.get("sellCurrency");
        String buyCurrency = (String) body.get("buyCurrency");
        BigDecimal sellAmount = new BigDecimal(body.get("sellAmount").toString());
        return R.ok(crossBorderService.createFxOrder(paymentId, orderId, sellCurrency, buyCurrency, sellAmount));
    }

    @GetMapping("/{id}")
    public R<FxOrder> getById(@PathVariable Long id) {
        return R.ok(crossBorderService.getFxOrder(id));
    }
}
