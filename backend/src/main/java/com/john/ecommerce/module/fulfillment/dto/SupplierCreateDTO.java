package com.john.ecommerce.module.fulfillment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SupplierCreateDTO {
    @NotBlank(message = "供应商名称不能为空")
    private String name;
    private String contactName;
    private String contactPhone;
    private String address;
}
