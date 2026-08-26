package com.john.ecommerce.module.payment.dto;

import lombok.Data;

@Data
public class LedgerCreditDTO {
    private Long amount;
    private String remark;
}
