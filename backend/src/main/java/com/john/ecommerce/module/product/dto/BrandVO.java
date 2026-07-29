package com.john.ecommerce.module.product.dto;

import lombok.Data;

@Data
public class BrandVO {
    private Long id;
    private String name;
    private String logo;
    private String description;
    private Integer sortOrder;
    private Integer status;
    private Long createdAt;
}
