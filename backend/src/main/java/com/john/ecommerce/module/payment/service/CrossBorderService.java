package com.john.ecommerce.module.payment.service;

import com.john.ecommerce.module.payment.entity.CustomsDeclaration;
import com.john.ecommerce.module.payment.entity.FxOrder;
import com.john.ecommerce.module.payment.mapper.CustomsDeclarationMapper;
import com.john.ecommerce.module.payment.mapper.FxOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CrossBorderService {

    private final CustomsDeclarationMapper customsMapper;
    private final FxOrderMapper fxOrderMapper;

    @Transactional
    public CustomsDeclaration declare(Long paymentId, Long orderId, String customsCode) {
        CustomsDeclaration cd = new CustomsDeclaration();
        cd.setDeclarationNo("CD" + UUID.randomUUID().toString().replace("-", "").substring(0, 20));
        cd.setPaymentId(paymentId);
        cd.setOrderId(orderId);
        cd.setCustomsCode(customsCode);
        cd.setStatus(0);
        cd.setPayload(Map.of("mock", true));
        customsMapper.insert(cd);
        return cd;
    }

    @Transactional
    public FxOrder createFxOrder(Long paymentId, Long orderId, String sellCurrency, String buyCurrency, java.math.BigDecimal sellAmount) {
        FxOrder fx = new FxOrder();
        fx.setFxNo("FX" + UUID.randomUUID().toString().replace("-", "").substring(0, 20));
        fx.setPaymentId(paymentId);
        fx.setOrderId(orderId);
        fx.setSellCurrency(sellCurrency);
        fx.setBuyCurrency(buyCurrency);
        fx.setSellAmount(sellAmount);
        fx.setStatus(0);
        fxOrderMapper.insert(fx);
        return fx;
    }

    public CustomsDeclaration getDeclaration(Long id) {
        return customsMapper.selectById(id);
    }

    public FxOrder getFxOrder(Long id) {
        return fxOrderMapper.selectById(id);
    }
}
