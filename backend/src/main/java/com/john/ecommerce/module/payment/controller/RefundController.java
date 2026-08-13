package com.john.ecommerce.module.payment.controller;

import com.john.ecommerce.common.module.ModuleCodes;
import com.john.ecommerce.common.module.RequiresModule;
import com.john.ecommerce.common.result.R;
import com.john.ecommerce.module.payment.service.RefundService;
import com.john.ecommerce.module.trade.dto.RefundApplyDTO;
import com.john.ecommerce.module.trade.entity.Refund;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/refund")
@RequiredArgsConstructor
@RequiresModule(ModuleCodes.PAYMENT)
public class RefundController {
    private final RefundService refundService;
    @PostMapping("/order/{orderId}")
    @PreAuthorize("hasRole('BUYER')")
    public R<Refund> apply(@PathVariable Long orderId, @Valid @RequestBody RefundApplyDTO dto) { return R.ok(refundService.apply(orderId, dto)); }
    @PutMapping("/{id}/audit")
    @PreAuthorize("hasRole('OPS')")
    public R<Refund> audit(@PathVariable Long id, @RequestParam boolean approved) { return R.ok(refundService.approve(id, approved)); }
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('BUYER','OPS')")
    public R<Refund> get(@PathVariable Long id) { return R.ok(refundService.get(id)); }
    @GetMapping
    @PreAuthorize("hasAnyRole('BUYER','OPS')")
    public R<Page<Refund>> list(@RequestParam(defaultValue = "1") int page,
                                @RequestParam(defaultValue = "20") int size) { return R.ok(refundService.list(page, size)); }
    @PostMapping("/callback")
    public R<Refund> callback(@RequestParam String refundNo,
                              @RequestParam boolean success,
                              @RequestParam(required = false) String channelRefundNo,
                              @RequestHeader("X-Payment-Timestamp") String timestamp,
                              @RequestHeader("X-Payment-Signature") String signature) {
        return R.ok(refundService.callback(refundNo, timestamp, signature, success, channelRefundNo));
    }
}
