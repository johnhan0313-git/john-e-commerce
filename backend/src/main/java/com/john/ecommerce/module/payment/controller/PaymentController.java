package com.john.ecommerce.module.payment.controller;

import com.john.ecommerce.common.module.ModuleCodes;
import com.john.ecommerce.common.module.RequiresModule;
import com.john.ecommerce.common.result.R;
import com.john.ecommerce.module.payment.dto.PaymentCreateDTO;
import com.john.ecommerce.module.payment.dto.PaymentVO;
import com.john.ecommerce.module.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
@RequiresModule(ModuleCodes.PAYMENT)
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public R<PaymentVO> create(@Valid @RequestBody PaymentCreateDTO dto) {
        return R.ok(paymentService.createPayment(dto));
    }

    @PostMapping("/mock-callback")
    public R<Void> mockCallback(@RequestParam String payNo) {
        paymentService.mockCallback(payNo);
        return R.ok();
    }

    @PostMapping("/callback")
    public R<Void> callback(@RequestParam String payNo,
                            @RequestHeader("X-Payment-Timestamp") String timestamp,
                            @RequestHeader("X-Payment-Signature") String signature) {
        paymentService.channelCallback(payNo, timestamp, signature);
        return R.ok();
    }

    @GetMapping("/{id}")
    public R<PaymentVO> getById(@PathVariable Long id) {
        return R.ok(paymentService.getById(id));
    }
}
