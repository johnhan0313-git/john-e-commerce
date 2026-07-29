package com.john.ecommerce.module.merchant.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class MerchantVO {
    private Long id;
    private Long userId;
    private String name;
    private String logo;
    private String licenseNo;
    private List<String> licenseImages;
    private String contactName;
    private String contactPhone;
    private Integer status;
    private String statusLabel;
    private BigDecimal commissionRate;
    private Long settledAt;
    private Long createdAt;
}
