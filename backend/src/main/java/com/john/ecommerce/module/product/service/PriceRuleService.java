package com.john.ecommerce.module.product.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.john.ecommerce.common.exception.BizException;
import com.john.ecommerce.module.product.dto.PriceRuleCreateDTO;
import com.john.ecommerce.module.product.dto.PriceRuleVO;
import com.john.ecommerce.module.product.entity.PriceRule;
import com.john.ecommerce.module.product.mapper.PriceRuleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PriceRuleService {

    private final PriceRuleMapper priceRuleMapper;

    public PriceRuleVO create(PriceRuleCreateDTO dto) {
        PriceRule rule = new PriceRule();
        rule.setSpuId(dto.getSpuId());
        rule.setSkuId(dto.getSkuId());
        rule.setRuleType(dto.getRuleType());
        rule.setMinQty(dto.getMinQty() != null ? dto.getMinQty() : 1);
        rule.setPrice(dto.getPrice());
        rule.setStartTime(dto.getStartTime());
        rule.setEndTime(dto.getEndTime());
        rule.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        priceRuleMapper.insert(rule);
        return toVO(rule);
    }

    public PriceRuleVO update(Long id, PriceRuleCreateDTO dto) {
        PriceRule rule = require(id);
        if (dto.getSpuId() != null) rule.setSpuId(dto.getSpuId());
        if (dto.getSkuId() != null) rule.setSkuId(dto.getSkuId());
        if (dto.getRuleType() != null) rule.setRuleType(dto.getRuleType());
        if (dto.getMinQty() != null) rule.setMinQty(dto.getMinQty());
        if (dto.getPrice() != null) rule.setPrice(dto.getPrice());
        if (dto.getStartTime() != null) rule.setStartTime(dto.getStartTime());
        if (dto.getEndTime() != null) rule.setEndTime(dto.getEndTime());
        if (dto.getStatus() != null) rule.setStatus(dto.getStatus());
        priceRuleMapper.updateById(rule);
        return toVO(rule);
    }

    public PriceRuleVO getById(Long id) {
        return toVO(require(id));
    }

    public List<PriceRuleVO> list(Long spuId, Long skuId) {
        return priceRuleMapper.selectList(new LambdaQueryWrapper<PriceRule>()
                        .eq(spuId != null, PriceRule::getSpuId, spuId)
                        .eq(skuId != null, PriceRule::getSkuId, skuId)
                        .orderByDesc(PriceRule::getCreatedAt))
                .stream().map(this::toVO).toList();
    }

    public void delete(Long id) {
        require(id);
        priceRuleMapper.deleteById(id);
    }

    private PriceRule require(Long id) {
        PriceRule rule = priceRuleMapper.selectById(id);
        if (rule == null) throw new BizException("价格规则不存在");
        return rule;
    }

    private PriceRuleVO toVO(PriceRule r) {
        PriceRuleVO vo = new PriceRuleVO();
        vo.setId(r.getId());
        vo.setSpuId(r.getSpuId());
        vo.setSkuId(r.getSkuId());
        vo.setRuleType(r.getRuleType());
        vo.setMinQty(r.getMinQty());
        vo.setPrice(r.getPrice());
        vo.setStartTime(r.getStartTime());
        vo.setEndTime(r.getEndTime());
        vo.setStatus(r.getStatus());
        vo.setCreatedAt(r.getCreatedAt());
        return vo;
    }
}
