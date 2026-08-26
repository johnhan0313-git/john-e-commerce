package com.john.ecommerce.module.payment.dto;

import lombok.Data;

@Data
public class SettlementVO {
    private Long id;
    private String settleNo;
    private Long settlementBillId;
    private Long merchantId;
    private Long shopId;
    private Long netAmount;
    private String currency;
    private Integer status;
    private Long settledAt;
    private Long createdAt;
}
