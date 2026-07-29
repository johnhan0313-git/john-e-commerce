package com.john.ecommerce.module.fulfillment.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.john.ecommerce.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_stock_order")
public class StockOrder extends BaseEntity {
    private String stockOrderNo;
    /** IN / OUT / ADJUST */
    private String orderType;
    /** PURCHASE / SALE / TRANSFER / MANUAL */
    private String bizType;
    private Long warehouseId;
    private String refNo;
    /** 0=draft 1=confirmed */
    private Integer status;
    private String remark;
    private Long confirmedAt;
}
