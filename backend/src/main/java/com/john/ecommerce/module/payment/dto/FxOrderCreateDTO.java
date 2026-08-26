package com.john.ecommerce.module.payment.dto;

import lombok.Data;

@Data
public class FxOrderCreateDTO {
    private Long paymentId;
    private Long orderId;
    private String sellCurrency;
    private String buyCurrency;
    private Long sellAmount;
}
