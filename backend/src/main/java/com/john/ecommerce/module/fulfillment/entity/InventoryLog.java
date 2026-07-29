package com.john.ecommerce.module.fulfillment.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.john.ecommerce.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_inventory_log")
public class InventoryLog extends BaseEntity {
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
}
