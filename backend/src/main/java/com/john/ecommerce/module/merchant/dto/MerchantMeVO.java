package com.john.ecommerce.module.merchant.dto;

import lombok.Data;

import java.util.List;

@Data
public class MerchantMeVO {
    private MerchantVO merchant;
    private List<ShopVO> shops;
    private ShopVO currentShop;
}
