package com.john.ecommerce.module.fulfillment.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.john.ecommerce.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_stock_transfer_item")
public class StockTransferItem extends BaseEntity {
    private Long transferId;
    private Long skuId;
    private Integer qty;
}
