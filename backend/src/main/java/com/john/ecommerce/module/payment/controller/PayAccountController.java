package com.john.ecommerce.module.payment.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.john.ecommerce.common.module.ModuleCodes;
import com.john.ecommerce.common.module.RequiresModule;
import com.john.ecommerce.common.result.R;
import com.john.ecommerce.module.payment.dto.PayAccountCreateDTO;
import com.john.ecommerce.module.payment.dto.PayAccountVO;
import com.john.ecommerce.module.payment.service.PayAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pay-account")
@RequiredArgsConstructor
@RequiresModule(ModuleCodes.PAYMENT)
@PreAuthorize("hasRole('OPS')")
public class PayAccountController {

    private final PayAccountService payAccountService;

    @PostMapping
    public R<PayAccountVO> create(@RequestBody PayAccountCreateDTO dto) {
        return R.ok(payAccountService.create(dto));
    }

    @GetMapping("/{id}")
    public R<PayAccountVO> getById(@PathVariable Long id) {
        return R.ok(payAccountService.getById(id));
    }

    @GetMapping
    public R<Page<PayAccountVO>> list(@RequestParam(defaultValue = "1") int page,
                                      @RequestParam(defaultValue = "20") int size) {
        return R.ok(payAccountService.list(page, size));
    }

    @PutMapping("/{id}")
    public R<PayAccountVO> update(@PathVariable Long id, @RequestBody PayAccountCreateDTO dto) {
        return R.ok(payAccountService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        payAccountService.delete(id);
        return R.ok();
    }
}
