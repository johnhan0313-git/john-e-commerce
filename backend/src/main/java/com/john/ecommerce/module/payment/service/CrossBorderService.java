package com.john.ecommerce.module.payment.service;

import com.john.ecommerce.common.exception.BizException;
import com.john.ecommerce.module.payment.dto.CustomsDeclareDTO;
import com.john.ecommerce.module.payment.dto.CustomsDeclarationVO;
import com.john.ecommerce.module.payment.dto.FxOrderCreateDTO;
import com.john.ecommerce.module.payment.dto.FxOrderVO;
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
    public CustomsDeclarationVO declare(CustomsDeclareDTO dto) {
        CustomsDeclaration cd = new CustomsDeclaration();
        cd.setDeclarationNo("CD" + UUID.randomUUID().toString().replace("-", "").substring(0, 20));
        cd.setPaymentId(dto.getPaymentId());
        cd.setOrderId(dto.getOrderId());
        cd.setCustomsCode(dto.getCustomsCode());
        cd.setStatus(0);
        cd.setPayload(Map.of("mock", true));
        customsMapper.insert(cd);
        return toCustomsVO(cd);
    }

    @Transactional
    public FxOrderVO createFxOrder(FxOrderCreateDTO dto) {
        FxOrder fx = new FxOrder();
        fx.setFxNo("FX" + UUID.randomUUID().toString().replace("-", "").substring(0, 20));
        fx.setPaymentId(dto.getPaymentId());
        fx.setOrderId(dto.getOrderId());
        fx.setSellCurrency(dto.getSellCurrency());
        fx.setBuyCurrency(dto.getBuyCurrency());
        fx.setSellAmount(dto.getSellAmount());
        fx.setStatus(0);
        fxOrderMapper.insert(fx);
        return toFxVO(fx);
    }

    public CustomsDeclarationVO getDeclaration(Long id) {
        CustomsDeclaration cd = customsMapper.selectById(id);
        if (cd == null) throw new BizException("报关单不存在");
        return toCustomsVO(cd);
    }

    public FxOrderVO getFxOrder(Long id) {
        FxOrder fx = fxOrderMapper.selectById(id);
        if (fx == null) throw new BizException("换汇单不存在");
        return toFxVO(fx);
    }

    private CustomsDeclarationVO toCustomsVO(CustomsDeclaration cd) {
        CustomsDeclarationVO vo = new CustomsDeclarationVO();
        vo.setId(cd.getId());
        vo.setDeclarationNo(cd.getDeclarationNo());
        vo.setPaymentId(cd.getPaymentId());
        vo.setOrderId(cd.getOrderId());
        vo.setCustomsCode(cd.getCustomsCode());
        vo.setStatus(cd.getStatus());
        vo.setDeclaredAt(cd.getDeclaredAt());
        vo.setChannelRefNo(cd.getChannelRefNo());
        vo.setPayload(cd.getPayload());
        vo.setCreatedAt(cd.getCreatedAt());
        return vo;
    }

    private FxOrderVO toFxVO(FxOrder fx) {
        FxOrderVO vo = new FxOrderVO();
        vo.setId(fx.getId());
        vo.setFxNo(fx.getFxNo());
        vo.setPaymentId(fx.getPaymentId());
        vo.setOrderId(fx.getOrderId());
        vo.setSellCurrency(fx.getSellCurrency());
        vo.setBuyCurrency(fx.getBuyCurrency());
        vo.setSellAmount(fx.getSellAmount());
        vo.setBuyAmount(fx.getBuyAmount());
        vo.setExchangeRate(fx.getExchangeRate());
        vo.setStatus(fx.getStatus());
        vo.setChannelRefNo(fx.getChannelRefNo());
        vo.setCompletedAt(fx.getCompletedAt());
        vo.setExtra(fx.getExtra());
        vo.setCreatedAt(fx.getCreatedAt());
        return vo;
    }
}
