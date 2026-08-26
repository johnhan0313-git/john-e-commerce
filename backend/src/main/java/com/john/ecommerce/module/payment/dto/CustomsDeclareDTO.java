package com.john.ecommerce.module.payment.dto;

import lombok.Data;

@Data
public class CustomsDeclareDTO {
    private Long paymentId;
    private Long orderId;
    private String customsCode;
}
