package com.john.ecommerce.module.fulfillment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WarehouseCreateDTO {
    @NotBlank(message = "仓库编码不能为空")
    private String code;
    @NotBlank(message = "仓库名称不能为空")
    private String name;
    private Long merchantId;
    private Long shopId;
    private String address;
}
