package com.john.ecommerce.module.fulfillment.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PurchaseOrderVO {
    private Long id;
    private String poNo;
    private Long supplierId;
    private Long warehouseId;
    private Long refActivityId;
    private String status;
    private Long totalAmount;
    private String remark;
    private Long approvedAt;
    private Long finishedAt;
    private Long createdAt;
    private List<ItemVO> items;

    @Data
    public static class ItemVO {
        private Long id;
        private Long skuId;
        private Integer qty;
        private Integer receivedQty;
        private Long price;
    }
}
