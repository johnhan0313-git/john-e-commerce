package com.john.ecommerce.module.payment.dto;

import lombok.Data;

@Data
public class LedgerAccountVO {
    private Long id;
    private String ownerType;
    private Long ownerId;
    private String accountType;
    private String currency;
    private Long balance;
    private Long frozen;
    private Long available;
    private Integer status;
    private Long createdAt;
}
