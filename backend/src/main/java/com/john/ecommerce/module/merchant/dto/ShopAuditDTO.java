package com.john.ecommerce.module.merchant.dto;

import lombok.Data;

@Data
public class ShopAuditDTO {
    private Boolean approved;
    private String remark;
}
