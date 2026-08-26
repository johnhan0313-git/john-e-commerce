package com.john.ecommerce.module.payment.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.john.ecommerce.common.exception.BizException;
import com.john.ecommerce.module.payment.dto.PayChannelConfigCreateDTO;
import com.john.ecommerce.module.payment.dto.PayChannelConfigVO;
import com.john.ecommerce.module.payment.entity.PayChannelConfig;
import com.john.ecommerce.module.payment.mapper.PayChannelConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PayChannelConfigService {

    private final PayChannelConfigMapper payChannelConfigMapper;

    public Page<PayChannelConfigVO> list(int page, int size) {
        Page<PayChannelConfig> p = payChannelConfigMapper.selectPage(new Page<>(page, size), null);
        return mapPage(p);
    }

    public PayChannelConfigVO getById(Long id) {
        PayChannelConfig entity = payChannelConfigMapper.selectById(id);
        if (entity == null) throw new BizException("渠道配置不存在");
        return toVO(entity);
    }

    @Transactional
    public PayChannelConfigVO create(PayChannelConfigCreateDTO dto) {
        PayChannelConfig entity = fromDto(dto);
        payChannelConfigMapper.insert(entity);
        return toVO(entity);
    }

    @Transactional
    public PayChannelConfigVO update(Long id, PayChannelConfigCreateDTO dto) {
        PayChannelConfig entity = fromDto(dto);
        entity.setId(id);
        payChannelConfigMapper.updateById(entity);
        return toVO(payChannelConfigMapper.selectById(id));
    }

    private Page<PayChannelConfigVO> mapPage(Page<PayChannelConfig> p) {
        Page<PayChannelConfigVO> result = new Page<>();
        result.setTotal(p.getTotal());
        result.setCurrent(p.getCurrent());
        result.setSize(p.getSize());
        result.setRecords(p.getRecords().stream().map(this::toVO).toList());
        return result;
    }

    private PayChannelConfig fromDto(PayChannelConfigCreateDTO dto) {
        PayChannelConfig entity = new PayChannelConfig();
        entity.setPayAccountId(dto.getPayAccountId());
        entity.setChannelType(dto.getChannelType());
        entity.setMchNo(dto.getMchNo());
        entity.setCredentials(dto.getCredentials());
        entity.setCapability(dto.getCapability());
        entity.setWeight(dto.getWeight());
        entity.setStatus(dto.getStatus());
        entity.setExtra(dto.getExtra());
        return entity;
    }

    private PayChannelConfigVO toVO(PayChannelConfig e) {
        PayChannelConfigVO vo = new PayChannelConfigVO();
        vo.setId(e.getId());
        vo.setPayAccountId(e.getPayAccountId());
        vo.setChannelType(e.getChannelType());
        vo.setMchNo(e.getMchNo());
        vo.setCredentials(e.getCredentials());
        vo.setCapability(e.getCapability());
        vo.setWeight(e.getWeight());
        vo.setStatus(e.getStatus());
        vo.setExtra(e.getExtra());
        vo.setCreatedAt(e.getCreatedAt());
        return vo;
    }
}
