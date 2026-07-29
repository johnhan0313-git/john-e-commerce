package com.john.ecommerce.module.fulfillment.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.john.ecommerce.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_purchase_order_item")
public class PurchaseOrderItem extends BaseEntity {
    private Long purchaseOrderId;
    private Long skuId;
    private Integer qty;
    private Integer receivedQty;
    private BigDecimal price;
}
