package com.john.ecommerce.module.trade.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class OrderGroupVO {
    private String orderGroupNo;
    private Integer orderCount;
    private Long totalAmount;
    private Long discountAmount;
    private Long payAmount;
    private String groupStatus;
    private List<OrderVO> orders;

    @Data
    public static class OrderItemVO {
        private Long id;
        private Long spuId;
        private Long skuId;
        private String skuName;
        private String skuImage;
        private Map<String, String> specValues;
        private Long price;
        private Integer quantity;
        private Long subtotal;
        private Long discountAmount;
        private Long payAmount;
    }
}
