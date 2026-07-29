package com.john.ecommerce.module-trade.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class OrderVO {
    private Long id;
    private String orderNo;
    private Long userId;
    private Long merchantId;
    private Integer orderType;
    private Integer status;
    private String statusLabel;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal payAmount;
    private Integer payType;
    private Long payTime;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private String remark;
    private Long campaignId;
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
    }
}
