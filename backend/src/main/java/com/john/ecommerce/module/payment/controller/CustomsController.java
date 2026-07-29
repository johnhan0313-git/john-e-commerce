package com.john.ecommerce.module.payment.controller;

import com.john.ecommerce.common.module.ModuleCodes;
import com.john.ecommerce.common.module.RequiresModule;
import com.john.ecommerce.common.result.R;
import com.john.ecommerce.module.payment.entity.CustomsDeclaration;
import com.john.ecommerce.module.payment.service.CrossBorderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/customs")
@RequiredArgsConstructor
@RequiresModule(ModuleCodes.CROSSBORDER)
public class CustomsController {

    private final CrossBorderService crossBorderService;

    @PostMapping("/declare")
    public R<CustomsDeclaration> declare(@RequestBody Map<String, Object> body) {
        Long paymentId = Long.valueOf(body.get("paymentId").toString());
        Long orderId = Long.valueOf(body.get("orderId").toString());
        String customsCode = (String) body.get("customsCode");
        return R.ok(crossBorderService.declare(paymentId, orderId, customsCode));
    }

    @GetMapping("/{id}")
    public R<CustomsDeclaration> getById(@PathVariable Long id) {
        return R.ok(crossBorderService.getDeclaration(id));
    }
}
