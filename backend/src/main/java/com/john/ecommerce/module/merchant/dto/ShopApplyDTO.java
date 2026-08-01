package com.john.ecommerce.module.merchant.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ShopApplyDTO {
    @NotBlank(message = "店铺名称不能为空")
    private String name;
    private String logo;
}
