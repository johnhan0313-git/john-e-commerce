package com.john.ecommerce.module.merchant.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.john.ecommerce.common.exception.BizException;
import com.john.ecommerce.module.merchant.dto.ShopVO;
import com.john.ecommerce.module.merchant.entity.Shop;
import com.john.ecommerce.module.merchant.mapper.ShopMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShopService {

    private final ShopMapper shopMapper;

    public Shop createDefaultShop(Long merchantId, String name, String logo) {
        Shop existing = shopMapper.selectOne(new LambdaQueryWrapper<Shop>()
                .eq(Shop::getMerchantId, merchantId)
                .last("LIMIT 1"));
        if (existing != null) return existing;

        Shop shop = new Shop();
        shop.setMerchantId(merchantId);
        shop.setName(name != null && !name.isBlank() ? name : "默认店铺");
        shop.setLogo(logo);
        shop.setStatus(1);
        shopMapper.insert(shop);
        return shop;
    }

    public ShopVO getById(Long id) {
        return toVO(require(id));
    }

    public Shop findDefaultByMerchantId(Long merchantId) {
        return shopMapper.selectOne(new LambdaQueryWrapper<Shop>()
                .eq(Shop::getMerchantId, merchantId)
                .eq(Shop::getStatus, 1)
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
            case 0 -> "停用";
            case 1 -> "营业";
            default -> "未知";
        };
    }
}
