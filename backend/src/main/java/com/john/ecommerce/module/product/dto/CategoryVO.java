package com.john.ecommerce.module.product.dto;

import lombok.Data;
import java.util.List;

@Data
public class CategoryVO {
    private Long id;
    private Long parentId;
    private String name;
    private Integer sortOrder;
    private Integer level;
    private List<CategoryVO> children;
    private Long createdAt;
}
