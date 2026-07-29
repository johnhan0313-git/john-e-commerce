package com.john.ecommerce.module.fulfillment.dto;

import lombok.Data;

import java.util.List;

@Data
public class StockOrderVO {
    private Long id;
    private String stockOrderNo;
    private String orderType;
    private String bizType;
    private Long warehouseId;
    private String refNo;
    private Integer status;
    private String remark;
    private Long confirmedAt;
    private Long createdAt;
    private List<ItemVO> items;

    @Data
    public static class ItemVO {
        private Long id;
        private Long skuId;
        private Integer qty;
        private Integer actualQty;
        private List<LotVO> lots;
    }

    @Data
    public static class LotVO {
        private Long id;
        private Long lotId;
        private String lotNo;
        private Integer qty;
    }
}
