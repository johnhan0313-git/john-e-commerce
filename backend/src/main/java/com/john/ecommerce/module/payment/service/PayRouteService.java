package com.john.ecommerce.module.payment.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.john.ecommerce.common.exception.BizException;
import com.john.ecommerce.module.payment.dto.PayRouteRuleCreateDTO;
import com.john.ecommerce.module.payment.dto.PayRouteRuleVO;
import com.john.ecommerce.module.payment.entity.PayRouteRule;
import com.john.ecommerce.module.payment.mapper.PayRouteRuleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PayRouteService {

    private final PayRouteRuleMapper payRouteRuleMapper;

    public Page<PayRouteRuleVO> list(int page, int size) {
        Page<PayRouteRule> p = payRouteRuleMapper.selectPage(new Page<>(page, size), null);
        return mapPage(p);
    }

    public PayRouteRuleVO getById(Long id) {
        PayRouteRule entity = payRouteRuleMapper.selectById(id);
        if (entity == null) throw new BizException("路由规则不存在");
        return toVO(entity);
    }

    @Transactional
    public PayRouteRuleVO create(PayRouteRuleCreateDTO dto) {
        PayRouteRule entity = fromDto(dto);
        payRouteRuleMapper.insert(entity);
        return toVO(entity);
    }

    @Transactional
    public PayRouteRuleVO update(Long id, PayRouteRuleCreateDTO dto) {
        PayRouteRule entity = fromDto(dto);
        entity.setId(id);
        payRouteRuleMapper.updateById(entity);
        return toVO(payRouteRuleMapper.selectById(id));
    }

    private Page<PayRouteRuleVO> mapPage(Page<PayRouteRule> p) {
        Page<PayRouteRuleVO> result = new Page<>();
        result.setTotal(p.getTotal());
        result.setCurrent(p.getCurrent());
        result.setSize(p.getSize());
        result.setRecords(p.getRecords().stream().map(this::toVO).toList());
        return result;
    }

    private PayRouteRule fromDto(PayRouteRuleCreateDTO dto) {
        PayRouteRule entity = new PayRouteRule();
        entity.setMethodCode(dto.getMethodCode());
        entity.setScene(dto.getScene());
        entity.setPayAccountId(dto.getPayAccountId());
        entity.setChannelType(dto.getChannelType());
        entity.setCondition(dto.getCondition());
        entity.setPriority(dto.getPriority());
        entity.setStatus(dto.getStatus());
        return entity;
    }

    private PayRouteRuleVO toVO(PayRouteRule e) {
        PayRouteRuleVO vo = new PayRouteRuleVO();
        vo.setId(e.getId());
        vo.setMethodCode(e.getMethodCode());
        vo.setScene(e.getScene());
        vo.setPayAccountId(e.getPayAccountId());
        vo.setChannelType(e.getChannelType());
        vo.setCondition(e.getCondition());
        vo.setPriority(e.getPriority());
        vo.setStatus(e.getStatus());
        vo.setCreatedAt(e.getCreatedAt());
        return vo;
    }
}
