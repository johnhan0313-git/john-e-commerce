package com.john.ecommerce.module.fulfillment.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.john.ecommerce.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_stock_lot")
public class StockLot extends BaseEntity {
    private Long warehouseId;
    private Long skuId;
    private String lotNo;
    private Integer available;
    private Integer locked;
    private Integer inTransit;
    @Version
    private Integer version;
    private Long productionDate;
    private Long expireDate;
    private Long inboundAt;
    private Long supplierId;
}
