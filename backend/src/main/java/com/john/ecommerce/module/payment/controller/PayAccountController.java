package com.john.ecommerce.module.payment.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.john.ecommerce.common.exception.BizException;
import com.john.ecommerce.common.module.ModuleCodes;
import com.john.ecommerce.common.module.RequiresModule;
import com.john.ecommerce.common.result.R;
import com.john.ecommerce.module.payment.entity.PayAccount;
import com.john.ecommerce.module.payment.mapper.PayAccountMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pay-account")
@RequiredArgsConstructor
@RequiresModule(ModuleCodes.PAYMENT)
public class PayAccountController {

    private final PayAccountMapper payAccountMapper;

    @PostMapping
    public R<PayAccount> create(@RequestBody PayAccount account) {
        payAccountMapper.insert(account);
        return R.ok(account);
    }

    @GetMapping("/{id}")
    public R<PayAccount> getById(@PathVariable Long id) {
        PayAccount account = payAccountMapper.selectById(id);
        if (account == null) throw new BizException("支付账户不存在");
        return R.ok(account);
    }

    @GetMapping
    public R<Page<PayAccount>> list(@RequestParam(defaultValue = "1") int page,
                                    @RequestParam(defaultValue = "20") int size) {
        return R.ok(payAccountMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<PayAccount>().orderByDesc(PayAccount::getCreatedAt)));
    }

    @PutMapping("/{id}")
    public R<PayAccount> update(@PathVariable Long id, @RequestBody PayAccount account) {
        PayAccount existing = payAccountMapper.selectById(id);
        if (existing == null) throw new BizException("支付账户不存在");
        account.setId(id);
        payAccountMapper.updateById(account);
        return R.ok(payAccountMapper.selectById(id));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        payAccountMapper.deleteById(id);
        return R.ok();
    }
}
