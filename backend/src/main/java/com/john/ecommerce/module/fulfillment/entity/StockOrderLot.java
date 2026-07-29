package com.john.ecommerce.module.fulfillment.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.john.ecommerce.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_stock_order_lot")
public class StockOrderLot extends BaseEntity {
    private Long stockOrderItemId;
    private Long lotId;
    private String lotNo;
    private Integer qty;
}
