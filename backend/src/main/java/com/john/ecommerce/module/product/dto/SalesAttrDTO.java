package com.john.ecommerce.module.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class SalesAttrDTO {
    @NotBlank(message = "规格名不能为空")
    private String name;
    @NotEmpty(message = "规格值不能为空")
    private List<String> values;
}
