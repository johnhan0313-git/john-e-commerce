package com.john.ecommerce.module.trade.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;

@Data
public class RefundApplyDTO {
    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal amount;
    private String reason;
    @Valid
    @NotEmpty(message = "退款商品不能为空")
    private List<Item> items;

    @Data
    public static class Item {
        @NotNull(message = "订单项不能为空")
        private Long orderItemId;
        @NotNull(message = "退款数量不能为空")
        @Min(value = 1, message = "退款数量至少为1")
        private Integer quantity;
    }
}
