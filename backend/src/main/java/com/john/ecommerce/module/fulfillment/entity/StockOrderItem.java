package com.john.ecommerce.module.fulfillment.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.john.ecommerce.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_stock_order_item")
public class StockOrderItem extends BaseEntity {
    private Long stockOrderId;
    private Long skuId;
    private Integer qty;
    private Integer actualQty;
}
