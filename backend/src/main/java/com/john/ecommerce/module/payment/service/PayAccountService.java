package com.john.ecommerce.module.payment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.john.ecommerce.common.exception.BizException;
import com.john.ecommerce.module.payment.dto.PayAccountCreateDTO;
import com.john.ecommerce.module.payment.dto.PayAccountVO;
import com.john.ecommerce.module.payment.entity.PayAccount;
import com.john.ecommerce.module.payment.mapper.PayAccountMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PayAccountService {

    private final PayAccountMapper payAccountMapper;

    @Transactional
    public PayAccountVO create(PayAccountCreateDTO dto) {
        PayAccount account = fromDto(dto);
        payAccountMapper.insert(account);
        return toVO(account);
    }

    public PayAccountVO getById(Long id) {
        PayAccount account = payAccountMapper.selectById(id);
        if (account == null) throw new BizException("支付账户不存在");
        return toVO(account);
    }

    public Page<PayAccountVO> list(int page, int size) {
        Page<PayAccount> p = payAccountMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<PayAccount>().orderByDesc(PayAccount::getCreatedAt));
        return mapPage(p);
    }

    @Transactional
    public PayAccountVO update(Long id, PayAccountCreateDTO dto) {
        PayAccount existing = payAccountMapper.selectById(id);
        if (existing == null) throw new BizException("支付账户不存在");
        PayAccount account = fromDto(dto);
        account.setId(id);
        payAccountMapper.updateById(account);
        return toVO(payAccountMapper.selectById(id));
    }

    @Transactional
    public void delete(Long id) {
        payAccountMapper.deleteById(id);
    }

    private Page<PayAccountVO> mapPage(Page<PayAccount> p) {
        Page<PayAccountVO> result = new Page<>();
        result.setTotal(p.getTotal());
        result.setCurrent(p.getCurrent());
        result.setSize(p.getSize());
        result.setRecords(p.getRecords().stream().map(this::toVO).toList());
        return result;
    }

    private PayAccount fromDto(PayAccountCreateDTO dto) {
        PayAccount account = new PayAccount();
        account.setAccountCode(dto.getAccountCode());
        account.setName(dto.getName());
        account.setOwnerType(dto.getOwnerType());
        account.setOwnerId(dto.getOwnerId());
        account.setCurrency(dto.getCurrency());
        account.setDefaultRoutePolicyId(dto.getDefaultRoutePolicyId());
        account.setStatus(dto.getStatus());
        account.setExtra(dto.getExtra());
        return account;
    }

    private PayAccountVO toVO(PayAccount a) {
        PayAccountVO vo = new PayAccountVO();
        vo.setId(a.getId());
        vo.setAccountCode(a.getAccountCode());
        vo.setName(a.getName());
        vo.setOwnerType(a.getOwnerType());
        vo.setOwnerId(a.getOwnerId());
        vo.setCurrency(a.getCurrency());
        vo.setDefaultRoutePolicyId(a.getDefaultRoutePolicyId());
        vo.setStatus(a.getStatus());
        vo.setExtra(a.getExtra());
        vo.setCreatedAt(a.getCreatedAt());
        return vo;
    }
}
