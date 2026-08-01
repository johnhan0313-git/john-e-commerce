package com.john.ecommerce.module.merchant.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class MerchantUpdateDTO {
    @NotBlank(message = "主体名称不能为空")
    private String name;
    private String logo;
    private String licenseNo;
    private List<String> licenseImages;
    @NotBlank(message = "联系人不能为空")
    private String contactName;
    @NotBlank(message = "联系电话不能为空")
    private String contactPhone;
}
