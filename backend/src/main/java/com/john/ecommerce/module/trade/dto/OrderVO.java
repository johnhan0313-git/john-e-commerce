package com.john.ecommerce.module.trade.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class OrderVO {
    private Long id;
    private String orderGroupNo;
    private String orderNo;
    private Long userId;
    private Long merchantId;
    private Long shopId;
    private Long warehouseId;
    private Integer orderType;
    private Integer status;
    private String statusLabel;
    private String splitReason;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal payAmount;
    private BigDecimal paidAmount;
    private Integer payStatus;
    private String payStatusLabel;
    private Integer payType;
    private Long payTime;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private String remark;
    private Long activityId;
    private Long cancelTime;
    private String cancelReason;
    private List<OrderItemVO> items;
    private Map<String, Object> extra;
    private Long createdAt;

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
