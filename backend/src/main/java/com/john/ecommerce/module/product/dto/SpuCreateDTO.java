package com.john.ecommerce.module.product.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;

@Data
public class SpuCreateDTO {
    @NotBlank(message = "商品名称不能为空")
    private String name;
    private String subtitle;
    private Long categoryId;
    private Long merchantId;
    private Long shopId;
    private Long brandId;
    private String productCode;
    private List<String> mainImages;
    private String detail;
    private Integer productType;
    private Integer sortOrder;
}
