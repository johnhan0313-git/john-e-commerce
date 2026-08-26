package com.john.ecommerce.module.trade.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.john.ecommerce.common.module.ModuleCodes;
import com.john.ecommerce.common.module.RequiresModule;
import com.john.ecommerce.common.result.R;
import com.john.ecommerce.module.trade.application.PlaceOrderApplication;
import com.john.ecommerce.module.trade.dto.OrderCreateDTO;
import com.john.ecommerce.module.trade.dto.OrderGroupVO;
import com.john.ecommerce.module.trade.dto.OrderVO;
import com.john.ecommerce.module.trade.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
@RequiresModule(ModuleCodes.TRADE)
public class OrderController {

    private final PlaceOrderApplication placeOrderApplication;
    private final OrderService orderService;

    @PostMapping
    public R<OrderGroupVO> create(@Valid @RequestBody OrderCreateDTO dto) {
        return R.ok(placeOrderApplication.create(dto));
    }

    @GetMapping("/group/{orderGroupNo}")
    public R<OrderGroupVO> getGroup(@PathVariable String orderGroupNo) {
        return R.ok(orderService.getGroup(orderGroupNo));
    }

    @GetMapping("/{id}")
    public R<OrderVO> getById(@PathVariable Long id) {
        return R.ok(orderService.getById(id));
    }

    @GetMapping
    public R<Page<OrderVO>> list(@RequestParam(defaultValue = "1") int page,
                                 @RequestParam(defaultValue = "20") int size,
                                 @RequestParam(required = false) Integer status,
                                 @RequestParam(required = false) Long shopId,
                                 @RequestParam(required = false) Long merchantId,
                                 @RequestParam(defaultValue = "true") boolean buyerScoped) {
        // 运营端传 buyerScoped=false 并可选 shopId/merchantId；买家端默认按当前用户过滤
        return R.ok(orderService.list(page, size, status, shopId, merchantId, buyerScoped));
    }

    @PutMapping("/{id}/status")
    public R<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        orderService.updateStatus(id, status);
        return R.ok();
    }
}
