package com.john.ecommerce.module-trade.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.john.ecommerce.common.result.R;
import com.john.ecommerce.module-trade.dto.OrderCreateDTO;
import com.john.ecommerce.module-trade.dto.OrderVO;
import com.john.ecommerce.module-trade.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public R<OrderVO> create(@Valid @RequestBody OrderCreateDTO dto) {
        return R.ok(orderService.create(dto));
    }

    @GetMapping("/{id}")
    public R<OrderVO> getById(@PathVariable Long id) {
        return R.ok(orderService.getById(id));
    }

    @GetMapping
    public R<Page<OrderVO>> list(@RequestParam(defaultValue = "1") int page,
                                 @RequestParam(defaultValue = "20") int size,
                                 @RequestParam(required = false) Integer status) {
        return R.ok(orderService.list(page, size, status));
    }

    @PutMapping("/{id}/status")
    public R<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        orderService.updateStatus(id, status);
        return R.ok();
    }
}
