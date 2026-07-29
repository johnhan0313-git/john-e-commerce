package com.john.ecommerce.module-trade.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class OrderCreateDTO {
    @NotEmpty(message = "订单项不能为空")
    private List<OrderItemDTO> items;
    private Integer orderType;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private String remark;
    private Long campaignId;
    private Long addressId;
    private Integer diningType;
    private String tableNo;

    @Data
    public static class OrderItemDTO {
        @NotNull(message = "SKU ID 不能为空")
        private Long skuId;
        @NotNull(message = "数量不能为空")
        private Integer quantity;
    }
}
