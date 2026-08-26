package com.john.ecommerce.module.payment.dto;

import lombok.Data;

@Data
public class LedgerFlowVO {
    private Long id;
    private Long ledgerAccountId;
    private Long txnId;
    private String direction;
    private Long amount;
    private Long balanceBefore;
    private Long balanceAfter;
    private String bizType;
    private String refType;
    private Long refId;
    private String remark;
    private Long createdAt;
}
