package com.john.ecommerce.module.fulfillment.dto;

import lombok.Data;

import java.util.List;

@Data
public class StockTransferVO {
    private Long id;
    private String transferNo;
    private Long fromWarehouseId;
    private Long toWarehouseId;
    private Integer status;
    private String remark;
    private Long shippedAt;
    private Long receivedAt;
    private Long createdAt;
    private List<ItemVO> items;

    @Data
    public static class ItemVO {
        private Long id;
        private Long skuId;
        private Integer qty;
    }
}
