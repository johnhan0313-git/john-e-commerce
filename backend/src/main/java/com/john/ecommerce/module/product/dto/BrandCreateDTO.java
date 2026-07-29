package com.john.ecommerce.module.product.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BrandCreateDTO {
    @NotBlank(message = "品牌名称不能为空")
    private String name;
    private String logo;
    private String description;
    private Integer sortOrder;
    private Integer status;
}
