package com.john.ecommerce.module.fulfillment.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class LogisticsCreateDTO {
    @NotNull(message = "订单不能为空")
    private Long orderId;
    private String provider;
    private String trackingNo;
    @NotEmpty(message = "物流明细不能为空")
    private List<Item> items;

    @Data
    public static class Item {
        @NotNull(message = "订单项不能为空")
        private Long orderItemId;
        @NotNull(message = "数量不能为空")
        private Integer qty;
    }
}
