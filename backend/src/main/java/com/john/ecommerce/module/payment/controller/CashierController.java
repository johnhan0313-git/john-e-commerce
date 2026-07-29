package com.john.ecommerce.module.payment.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.john.ecommerce.common.module.ModuleCodes;
import com.john.ecommerce.common.module.RequiresModule;
import com.john.ecommerce.common.result.R;
import com.john.ecommerce.module.payment.entity.PayMethod;
import com.john.ecommerce.module.payment.mapper.PayMethodMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/cashier")
@RequiredArgsConstructor
@RequiresModule(ModuleCodes.PAYMENT)
public class CashierController {

    private final PayMethodMapper payMethodMapper;

    @GetMapping("/methods")
    public R<List<PayMethod>> methods() {
        List<PayMethod> list = payMethodMapper.selectList(new LambdaQueryWrapper<PayMethod>()
                .eq(PayMethod::getStatus, 1)
                .orderByAsc(PayMethod::getSortOrder));
        return R.ok(list);
    }
}
