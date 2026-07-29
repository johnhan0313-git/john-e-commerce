package com.john.ecommerce.module.fulfillment.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.john.ecommerce.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_stock_transfer")
public class StockTransfer extends BaseEntity {
    private String transferNo;
    private Long fromWarehouseId;
    private Long toWarehouseId;
    /** 0=pending 1=shipped 2=received */
    private Integer status;
    private String remark;
    private Long shippedAt;
    private Long receivedAt;
}
