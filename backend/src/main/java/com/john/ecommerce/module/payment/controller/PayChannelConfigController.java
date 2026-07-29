package com.john.ecommerce.module.payment.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.john.ecommerce.common.module.ModuleCodes;
import com.john.ecommerce.common.module.RequiresModule;
import com.john.ecommerce.common.result.R;
import com.john.ecommerce.module.payment.entity.PayChannelConfig;
import com.john.ecommerce.module.payment.mapper.PayChannelConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pay-channel-config")
@RequiredArgsConstructor
@RequiresModule(ModuleCodes.PAYMENT)
public class PayChannelConfigController {

    private final PayChannelConfigMapper payChannelConfigMapper;

    @GetMapping
    public R<Page<PayChannelConfig>> list(@RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "20") int size) {
        return R.ok(payChannelConfigMapper.selectPage(new Page<>(page, size), null));
    }

    @GetMapping("/{id}")
    public R<PayChannelConfig> getById(@PathVariable Long id) {
        return R.ok(payChannelConfigMapper.selectById(id));
    }

    @PostMapping
    public R<PayChannelConfig> create(@RequestBody PayChannelConfig entity) {
        payChannelConfigMapper.insert(entity);
        return R.ok(entity);
    }

    @PutMapping("/{id}")
    public R<PayChannelConfig> update(@PathVariable Long id, @RequestBody PayChannelConfig entity) {
        entity.setId(id);
        payChannelConfigMapper.updateById(entity);
        return R.ok(entity);
    }
}
