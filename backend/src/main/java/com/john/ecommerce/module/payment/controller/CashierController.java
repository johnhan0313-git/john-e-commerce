package com.john.ecommerce.module.payment.controller;

import com.john.ecommerce.common.module.ModuleCodes;
import com.john.ecommerce.common.module.RequiresModule;
import com.john.ecommerce.common.result.R;
import com.john.ecommerce.module.payment.dto.PayMethodVO;
import com.john.ecommerce.module.payment.service.CashierService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/cashier")
@RequiredArgsConstructor
@RequiresModule(ModuleCodes.PAYMENT)
public class CashierController {

    private final CashierService cashierService;

    @GetMapping("/methods")
    public R<List<PayMethodVO>> methods() {
        return R.ok(cashierService.listActiveMethods());
    }
}
