package com.john.ecommerce.module.payment.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.john.ecommerce.common.module.ModuleCodes;
import com.john.ecommerce.common.module.RequiresModule;
import com.john.ecommerce.common.result.R;
import com.john.ecommerce.module.payment.dto.PayChannelConfigCreateDTO;
import com.john.ecommerce.module.payment.dto.PayChannelConfigVO;
import com.john.ecommerce.module.payment.service.PayChannelConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pay-channel-config")
@RequiredArgsConstructor
@RequiresModule(ModuleCodes.PAYMENT)
@PreAuthorize("hasRole('OPS')")
public class PayChannelConfigController {

    private final PayChannelConfigService payChannelConfigService;

    @GetMapping
    public R<Page<PayChannelConfigVO>> list(@RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "20") int size) {
        return R.ok(payChannelConfigService.list(page, size));
    }

    @GetMapping("/{id}")
    public R<PayChannelConfigVO> getById(@PathVariable Long id) {
        return R.ok(payChannelConfigService.getById(id));
    }

    @PostMapping
    public R<PayChannelConfigVO> create(@RequestBody PayChannelConfigCreateDTO dto) {
        return R.ok(payChannelConfigService.create(dto));
    }

    @PutMapping("/{id}")
    public R<PayChannelConfigVO> update(@PathVariable Long id, @RequestBody PayChannelConfigCreateDTO dto) {
        return R.ok(payChannelConfigService.update(id, dto));
    }
}
