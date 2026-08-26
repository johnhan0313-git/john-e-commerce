package com.john.ecommerce.module.payment.dto;

import lombok.Data;

@Data
public class LedgerTxnVO {
    private Long id;
    private String txnNo;
    private String txnType;
    private Long amount;
    private String currency;
    private Integer status;
    private String bizType;
    private String refType;
    private Long refId;
    private String remark;
    private Long createdAt;
}
