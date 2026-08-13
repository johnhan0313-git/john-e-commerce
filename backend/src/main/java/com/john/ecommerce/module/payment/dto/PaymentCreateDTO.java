package com.john.ecommerce.module.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
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
        @NotNull(message = "订单不能为空")
        private Long orderId;
        @NotNull(message = "支付金额不能为空")
        @DecimalMin(value = "0.01", message = "支付金额必须大于0")
        private BigDecimal amount;
    }
}
