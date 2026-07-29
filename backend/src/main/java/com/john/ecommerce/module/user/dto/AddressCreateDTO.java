package com.john.ecommerce.module.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddressCreateDTO {
    @NotBlank(message = "收件人不能为空")
    private String name;
    @NotBlank(message = "手机号不能为空")
    private String phone;
    private String province;
    private String city;
    private String district;
    @NotBlank(message = "详细地址不能为空")
    private String detail;
    private String postalCode;
    private Boolean isDefault;
}
