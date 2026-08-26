package com.john.ecommerce.module.payment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.john.ecommerce.module.payment.dto.PayMethodVO;
import com.john.ecommerce.module.payment.entity.PayMethod;
import com.john.ecommerce.module.payment.mapper.PayMethodMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CashierService {

    private final PayMethodMapper payMethodMapper;

    public List<PayMethodVO> listActiveMethods() {
        return payMethodMapper.selectList(new LambdaQueryWrapper<PayMethod>()
                        .eq(PayMethod::getStatus, 1)
                        .orderByAsc(PayMethod::getSortOrder))
                .stream().map(this::toVO).toList();
    }

    private PayMethodVO toVO(PayMethod m) {
        PayMethodVO vo = new PayMethodVO();
        vo.setId(m.getId());
        vo.setMethodCode(m.getMethodCode());
        vo.setName(m.getName());
        vo.setIconUrl(m.getIconUrl());
        vo.setSortOrder(m.getSortOrder());
        vo.setStatus(m.getStatus());
        vo.setExtra(m.getExtra());
        return vo;
    }
}
