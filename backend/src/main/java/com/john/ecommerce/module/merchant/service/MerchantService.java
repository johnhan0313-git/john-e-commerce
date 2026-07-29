package com.john.ecommerce.module.merchant.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.john.ecommerce.common.context.UserContext;
import com.john.ecommerce.common.exception.BizException;
import com.john.ecommerce.module.merchant.dto.MerchantApplyDTO;
import com.john.ecommerce.module.merchant.dto.MerchantAuditDTO;
import com.john.ecommerce.module.merchant.dto.MerchantVO;
import com.john.ecommerce.module.merchant.entity.Merchant;
import com.john.ecommerce.module.merchant.mapper.MerchantMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class MerchantService {

    private final MerchantMapper merchantMapper;

    public MerchantVO apply(MerchantApplyDTO dto) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) throw new BizException("用户未登录");
        Merchant existing = merchantMapper.selectOne(new LambdaQueryWrapper<Merchant>()
                .eq(Merchant::getUserId, userId));
        if (existing != null) throw new BizException("已提交入驻申请");

        Merchant merchant = new Merchant();
        merchant.setUserId(userId);
        merchant.setName(dto.getName());
        merchant.setLogo(dto.getLogo());
        merchant.setLicenseNo(dto.getLicenseNo());
        merchant.setLicenseImages(dto.getLicenseImages());
        merchant.setContactName(dto.getContactName());
        merchant.setContactPhone(dto.getContactPhone());
        merchant.setCommissionRate(dto.getCommissionRate() != null ? dto.getCommissionRate() : BigDecimal.ZERO);
        merchant.setStatus(0);
        merchantMapper.insert(merchant);
        return toVO(merchant);
    }

    public MerchantVO audit(Long id, MerchantAuditDTO dto) {
        Merchant merchant = require(id);
        if (merchant.getStatus() != 0) throw new BizException("当前状态不可审核");
        if (Boolean.TRUE.equals(dto.getApproved())) {
            merchant.setStatus(1);
            merchant.setSettledAt(System.currentTimeMillis());
        } else {
            merchant.setStatus(2);
        }
        merchantMapper.updateById(merchant);
        return toVO(merchant);
    }

    public MerchantVO getById(Long id) {
        return toVO(require(id));
    }

    public Page<MerchantVO> list(int page, int size, Integer status) {
        Page<Merchant> p = merchantMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Merchant>()
                        .eq(status != null, Merchant::getStatus, status)
                        .orderByDesc(Merchant::getCreatedAt));
        Page<MerchantVO> result = new Page<>();
        result.setTotal(p.getTotal());
        result.setCurrent(p.getCurrent());
        result.setSize(p.getSize());
        result.setRecords(p.getRecords().stream().map(this::toVO).toList());
        return result;
    }

    private Merchant require(Long id) {
        Merchant merchant = merchantMapper.selectById(id);
        if (merchant == null) throw new BizException("商家不存在");
        return merchant;
    }

    private MerchantVO toVO(Merchant m) {
        MerchantVO vo = new MerchantVO();
        vo.setId(m.getId());
        vo.setUserId(m.getUserId());
        vo.setName(m.getName());
        vo.setLogo(m.getLogo());
        vo.setLicenseNo(m.getLicenseNo());
        vo.setLicenseImages(m.getLicenseImages());
        vo.setContactName(m.getContactName());
        vo.setContactPhone(m.getContactPhone());
        vo.setStatus(m.getStatus());
        vo.setStatusLabel(statusLabel(m.getStatus()));
        vo.setCommissionRate(m.getCommissionRate());
        vo.setSettledAt(m.getSettledAt());
        vo.setCreatedAt(m.getCreatedAt());
        return vo;
    }

    private String statusLabel(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 0 -> "待审核";
            case 1 -> "已通过";
            case 2 -> "已拒绝";
            default -> "未知";
        };
    }
}
