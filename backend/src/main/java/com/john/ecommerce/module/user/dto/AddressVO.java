package com.john.ecommerce.module.user.dto;

import lombok.Data;

@Data
public class AddressVO {
    private Long id;
    private Long userId;
    private String name;
    private String phone;
    private String province;
    private String city;
    private String district;
    private String detail;
    private String postalCode;
    private Boolean isDefault;
    private Long createdAt;
}
