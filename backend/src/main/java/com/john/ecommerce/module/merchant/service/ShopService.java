package com.john.ecommerce.module.merchant.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.john.ecommerce.common.exception.BizException;
import com.john.ecommerce.module.merchant.dto.ShopApplyDTO;
import com.john.ecommerce.module.merchant.dto.ShopAuditDTO;
import com.john.ecommerce.module.merchant.dto.ShopUpdateDTO;
import com.john.ecommerce.module.merchant.dto.ShopVO;
import com.john.ecommerce.module.merchant.entity.Merchant;
import com.john.ecommerce.module.merchant.entity.Shop;
import com.john.ecommerce.module.merchant.mapper.ShopMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShopService {

    public static final int STATUS_PENDING = 0;
    public static final int STATUS_OPEN = 1;
    public static final int STATUS_REJECTED = 2;
    public static final int STATUS_DISABLED = 3;

    private final ShopMapper shopMapper;

    /** Bootstrap first shop when merchant onboarding is approved. */
    public Shop createDefaultShop(Long merchantId, String name, String logo) {
        long count = shopMapper.selectCount(new LambdaQueryWrapper<Shop>()
                .eq(Shop::getMerchantId, merchantId));
        if (count > 0) {
            return findDefaultByMerchantId(merchantId);
        }

        Shop shop = new Shop();
        shop.setMerchantId(merchantId);
        shop.setName(name != null && !name.isBlank() ? name : "默认店铺");
        shop.setLogo(logo);
        shop.setStatus(STATUS_OPEN);
        shopMapper.insert(shop);
        return shop;
    }

    @Transactional
    public ShopVO apply(Merchant merchant, ShopApplyDTO dto) {
        Shop shop = new Shop();
        shop.setMerchantId(merchant.getId());
        shop.setName(dto.getName().trim());
        shop.setLogo(dto.getLogo());
        shop.setStatus(STATUS_PENDING);
        shopMapper.insert(shop);
        return toVO(shop);
    }

    /**
     * Seller updates own shop name/logo. Rejected shops go back to pending for re-audit.
     */
    @Transactional
    public ShopVO updateOwned(Merchant merchant, Long shopId, ShopUpdateDTO dto) {
        Shop shop = requireOwned(shopId, merchant.getId());
        shop.setName(dto.getName().trim());
        shop.setLogo(blankToNull(dto.getLogo()));
        if (shop.getStatus() != null && shop.getStatus() == STATUS_REJECTED) {
            shop.setStatus(STATUS_PENDING);
        }
        shopMapper.updateById(shop);
        return toVO(shop);
    }

    @Transactional
    public ShopVO audit(Long id, ShopAuditDTO dto) {
        Shop shop = require(id);
        if (shop.getStatus() == null || shop.getStatus() != STATUS_PENDING) {
            throw new BizException("当前状态不可审核");
        }
        if (Boolean.TRUE.equals(dto.getApproved())) {
            shop.setStatus(STATUS_OPEN);
        } else {
            shop.setStatus(STATUS_REJECTED);
        }
        shopMapper.updateById(shop);
        return toVO(shop);
    }

    public List<ShopVO> listByMerchant(Long merchantId) {
        return shopMapper.selectList(new LambdaQueryWrapper<Shop>()
                        .eq(Shop::getMerchantId, merchantId)
                        .orderByAsc(Shop::getId))
                .stream()
                .map(this::toVO)
                .toList();
    }

    public Shop findDefaultByMerchantId(Long merchantId) {
        return shopMapper.selectOne(new LambdaQueryWrapper<Shop>()
                .eq(Shop::getMerchantId, merchantId)
                .eq(Shop::getStatus, STATUS_OPEN)
                .orderByAsc(Shop::getId)
                .last("LIMIT 1"));
    }

    public Shop requireOwned(Long shopId, Long merchantId) {
        Shop shop = require(shopId);
        if (!merchantId.equals(shop.getMerchantId())) {
            throw new BizException("店铺不属于当前卖家");
        }
        return shop;
    }

    public Shop requireOwnedOpen(Long shopId, Long merchantId) {
        Shop shop = requireOwned(shopId, merchantId);
        if (shop.getStatus() == null || shop.getStatus() != STATUS_OPEN) {
            throw new BizException("店铺未营业");
        }
        return shop;
    }

    public Page<ShopVO> list(int page, int size, Long merchantId, Integer status) {
        Page<Shop> p = shopMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Shop>()
                        .eq(merchantId != null, Shop::getMerchantId, merchantId)
                        .eq(status != null, Shop::getStatus, status)
                        .orderByDesc(Shop::getCreatedAt));
        Page<ShopVO> result = new Page<>();
        result.setTotal(p.getTotal());
        result.setCurrent(p.getCurrent());
        result.setSize(p.getSize());
        result.setRecords(p.getRecords().stream().map(this::toVO).toList());
        return result;
    }

    public ShopVO getById(Long id) {
        return toVO(require(id));
    }

    public Shop require(Long id) {
        Shop shop = shopMapper.selectById(id);
        if (shop == null) throw new BizException("店铺不存在");
        return shop;
    }

    public ShopVO toVO(Shop s) {
        ShopVO vo = new ShopVO();
        vo.setId(s.getId());
        vo.setMerchantId(s.getMerchantId());
        vo.setName(s.getName());
        vo.setLogo(s.getLogo());
        vo.setStatus(s.getStatus());
        vo.setStatusLabel(statusLabel(s.getStatus()));
        vo.setExtra(s.getExtra());
        vo.setCreatedAt(s.getCreatedAt());
        return vo;
    }

    private String statusLabel(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case STATUS_PENDING -> "待审核";
            case STATUS_OPEN -> "营业";
            case STATUS_REJECTED -> "已拒绝";
            case STATUS_DISABLED -> "停用";
            default -> "未知";
        };
    }

    private static String blankToNull(String s) {
        if (s == null || s.isBlank()) return null;
        return s.trim();
    }
}
