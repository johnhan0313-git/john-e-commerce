package com.john.ecommerce.module.payment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.john.ecommerce.module.payment.entity.PayMethod;
import com.john.ecommerce.module.payment.mapper.PayMethodMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CashierService {

    private final PayMethodMapper payMethodMapper;

    public List<PayMethod> listMethods() {
        return payMethodMapper.selectList(
                new LambdaQueryWrapper<PayMethod>()
                        .eq(PayMethod::getStatus, 1)
                        .orderByAsc(PayMethod::getSortOrder));
    }
}
