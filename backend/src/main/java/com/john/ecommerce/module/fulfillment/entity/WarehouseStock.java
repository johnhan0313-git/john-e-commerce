package com.john.ecommerce.module.fulfillment.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.john.ecommerce.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_warehouse_stock")
public class WarehouseStock extends BaseEntity {
    private Long warehouseId;
    private Long skuId;
    private Integer available;
    private Integer locked;
    private Integer inTransit;
    @Version
    private Integer version;
}
