package com.john.ecommerce.module.trade.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class OrderGroupVO {
    private String orderGroupNo;
    private Integer orderCount;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal payAmount;
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
        private BigDecimal price;
        private Integer quantity;
        private BigDecimal subtotal;
        private BigDecimal discountAmount;
        private BigDecimal payAmount;
    }
}
