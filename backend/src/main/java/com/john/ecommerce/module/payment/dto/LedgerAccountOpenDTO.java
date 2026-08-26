package com.john.ecommerce.module.payment.dto;

import lombok.Data;

@Data
public class LedgerAccountOpenDTO {
    private String ownerType;
    private Long ownerId;
    private String accountType;
    private String currency;
}
