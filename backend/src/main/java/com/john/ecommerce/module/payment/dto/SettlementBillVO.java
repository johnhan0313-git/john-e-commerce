package com.john.ecommerce.module.payment.dto;

import lombok.Data;

@Data
public class SettlementBillVO {
    private Long id;
    private String billNo;
    private Long merchantId;
    private String payeeType;
    private Long payeeId;
    private Long periodStart;
    private Long periodEnd;
    private String currency;
    private Long billAmount;
    private Long preSettleAmount;
    private String status;
    private Integer settleStatus;
    private Long createdAt;
}
