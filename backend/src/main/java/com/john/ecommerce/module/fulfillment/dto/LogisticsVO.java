package com.john.ecommerce.module.fulfillment.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class LogisticsVO {
    private Long id;
    private Long orderId;
    private String logisticsNo;
    private String provider;
    private String trackingNo;
    private Integer status;
    private Long shippedAt;
    private Long deliveredAt;
    private Map<String, Object> extra;
    private Long createdAt;
    private List<ItemVO> items;

    @Data
    public static class ItemVO {
        private Long id;
        private Long orderItemId;
        private Integer qty;
    }
}
