package com.john.ecommerce.module.merchant.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class MerchantApplyDTO {
    @NotBlank(message = "商家名称不能为空")
    private String name;
    private String logo;
    private String licenseNo;
    private List<String> licenseImages;
    private String contactName;
    private String contactPhone;
    private BigDecimal commissionRate;
}
