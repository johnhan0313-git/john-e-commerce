package com.john.ecommerce.module.fulfillment.controller;

import com.john.ecommerce.common.module.ModuleCodes;
import com.john.ecommerce.common.module.RequiresModule;
import com.john.ecommerce.common.result.R;
import com.john.ecommerce.module.fulfillment.dto.LogisticsCreateDTO;
import com.john.ecommerce.module.fulfillment.dto.LogisticsVO;
import com.john.ecommerce.module.fulfillment.dto.LogisticsWebhookDTO;
import com.john.ecommerce.module.fulfillment.service.LogisticsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/logistics")
@RequiredArgsConstructor
public class LogisticsController {

    private final LogisticsService logisticsService;

    @PostMapping
    @RequiresModule(ModuleCodes.FULFILLMENT)
    public R<LogisticsVO> createShipment(@Valid @RequestBody LogisticsCreateDTO dto) {
        return R.ok(logisticsService.createShipment(dto));
    }

    @GetMapping("/order/{orderId}")
    @RequiresModule(ModuleCodes.FULFILLMENT)
    public R<LogisticsVO> getByOrderId(@PathVariable Long orderId) {
        return R.ok(logisticsService.getByOrderId(orderId));
    }

    /** 承运商回调：无 JWT，不做模块门禁（Security 已 permitAll）。 */
    @PostMapping("/webhook/{trackingNo}")
    public R<Void> webhook(@PathVariable String trackingNo, @RequestBody LogisticsWebhookDTO dto,
                           @RequestHeader("X-Logistics-Timestamp") String timestamp,
                           @RequestHeader("X-Logistics-Signature") String signature) {
        logisticsService.webhook(trackingNo, dto, timestamp, signature);
        return R.ok(null);
    }

    @PutMapping("/confirm-receipt/{orderId}")
    @RequiresModule(ModuleCodes.FULFILLMENT)
    public R<Void> confirmReceipt(@PathVariable Long orderId) {
        logisticsService.confirmReceipt(orderId);
        return R.ok(null);
    }
}
