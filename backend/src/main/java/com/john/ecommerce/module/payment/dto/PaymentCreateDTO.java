package com.john.ecommerce.module.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PaymentCreateDTO {
    @NotBlank(message = "支付方式不能为空")
    private String methodCode;
    @NotEmpty(message = "支付项不能为空")
    private List<Item> items;
    private String currency;

    @Data
    public static class Item {
        private Long orderId;
        private BigDecimal amount;
    }
}
