package com.john.ecommerce.module.payment.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.john.ecommerce.common.module.ModuleCodes;
import com.john.ecommerce.common.module.RequiresModule;
import com.john.ecommerce.common.result.R;
import com.john.ecommerce.module.payment.entity.PayRouteRule;
import com.john.ecommerce.module.payment.mapper.PayRouteRuleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pay-route")
@RequiredArgsConstructor
@RequiresModule(ModuleCodes.PAYMENT)
public class PayRouteController {

    private final PayRouteRuleMapper payRouteRuleMapper;

    @GetMapping
    public R<Page<PayRouteRule>> list(@RequestParam(defaultValue = "1") int page,
                                     @RequestParam(defaultValue = "20") int size) {
        return R.ok(payRouteRuleMapper.selectPage(new Page<>(page, size), null));
    }

    @GetMapping("/{id}")
    public R<PayRouteRule> getById(@PathVariable Long id) {
        return R.ok(payRouteRuleMapper.selectById(id));
    }

    @PostMapping
    public R<PayRouteRule> create(@RequestBody PayRouteRule entity) {
        payRouteRuleMapper.insert(entity);
        return R.ok(entity);
    }

    @PutMapping("/{id}")
    public R<PayRouteRule> update(@PathVariable Long id, @RequestBody PayRouteRule entity) {
        entity.setId(id);
        payRouteRuleMapper.updateById(entity);
        return R.ok(entity);
    }
}
