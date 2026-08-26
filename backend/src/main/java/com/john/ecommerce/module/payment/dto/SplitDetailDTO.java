package com.john.ecommerce.module.payment.dto;

import lombok.Data;

@Data
public class SplitDetailDTO {
    private String receiverType;
    private Long receiverId;
    private String receiverAccount;
    private Long amount;
    private String description;
}
