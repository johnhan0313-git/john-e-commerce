package com.john.ecommerce.module.merchant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ShopUpdateDTO {
    @NotBlank(message = "店铺名称不能为空")
    @Size(max = 100, message = "店铺名称过长")
    private String name;

    @Size(max = 512, message = "Logo URL 过长")
    private String logo;
}
