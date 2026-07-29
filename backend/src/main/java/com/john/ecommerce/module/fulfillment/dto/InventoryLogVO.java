package com.john.ecommerce.module.fulfillment.dto;

import lombok.Data;

@Data
public class InventoryLogVO {
    private Long id;
    private Long warehouseId;
    private Long skuId;
    private String lotNo;
    private String changeType;
    private Integer changeQty;
    private Integer beforeQty;
    private Integer afterQty;
    private String refType;
    private Long refId;
    private String remark;
    private Long createdAt;
}
