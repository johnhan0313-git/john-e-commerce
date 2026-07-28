package com.john.ecommerce.module-product.dto;

import lombok.Data;
import java.util.List;

@Data
public class SpuVO {
    private Long id;
    private Long merchantId;
    private Long categoryId;
    private String name;
    private String subtitle;
    private List<String> mainImages;
    private String detail;
    private Integer productType;
    private Integer status;
    private Integer sales;
    private Long createdAt;
}
