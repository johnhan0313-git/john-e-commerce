package com.john.ecommerce.module.payment.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.john.ecommerce.common.module.ModuleCodes;
import com.john.ecommerce.common.module.RequiresModule;
import com.john.ecommerce.common.result.R;
import com.john.ecommerce.module.payment.application.RefundApplication;
import com.john.ecommerce.module.payment.dto.RefundVO;
import com.john.ecommerce.module.trade.dto.RefundApplyDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/refund")
@RequiredArgsConstructor
@RequiresModule(ModuleCodes.PAYMENT)
public class RefundController {

    private final RefundApplication refundApplication;

    @PostMapping("/order/{orderId}")
    @PreAuthorize("hasRole('BUYER')")
    public R<RefundVO> apply(@PathVariable Long orderId, @Valid @RequestBody RefundApplyDTO dto) {
        return R.ok(refundApplication.apply(orderId, dto));
    }

    @PutMapping("/{id}/audit")
    @PreAuthorize("hasRole('OPS')")
    public R<RefundVO> audit(@PathVariable Long id, @RequestParam boolean approved) {
        return R.ok(refundApplication.approve(id, approved));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('BUYER','OPS')")
    public R<RefundVO> get(@PathVariable Long id) {
        return R.ok(refundApplication.get(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('BUYER','OPS')")
    public R<Page<RefundVO>> list(@RequestParam(defaultValue = "1") int page,
                                  @RequestParam(defaultValue = "20") int size) {
        return R.ok(refundApplication.list(page, size));
    }

    @PostMapping("/callback")
    public R<RefundVO> callback(@RequestParam String refundNo,
                                @RequestParam boolean success,
                                @RequestParam(required = false) String channelRefundNo,
                                @RequestHeader("X-Payment-Timestamp") String timestamp,
                                @RequestHeader("X-Payment-Signature") String signature) {
        return R.ok(refundApplication.callback(refundNo, timestamp, signature, success, channelRefundNo));
    }
}
