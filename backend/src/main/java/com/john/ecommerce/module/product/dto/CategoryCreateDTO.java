package com.john.ecommerce.module.product.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryCreateDTO {
    private Long parentId;
    @NotBlank(message = "类目名称不能为空")
    private String name;
    private Integer sortOrder;
    private Integer level;
}
