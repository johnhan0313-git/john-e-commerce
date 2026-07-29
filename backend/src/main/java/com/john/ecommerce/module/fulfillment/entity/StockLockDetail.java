package com.john.ecommerce.module.fulfillment.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.john.ecommerce.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_stock_lock_detail")
public class StockLockDetail extends BaseEntity {
    private Long orderId;
    private Long orderItemId;
    private Long warehouseId;
    private Long skuId;
    private Long lotId;
    private String lotNo;
    private Integer qty;
    /** 1=locked 0=released */
    private Integer status;
}
