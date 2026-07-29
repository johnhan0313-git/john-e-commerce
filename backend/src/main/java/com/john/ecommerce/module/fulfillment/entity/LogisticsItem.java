package com.john.ecommerce.module.fulfillment.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.john.ecommerce.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_logistics_item")
public class LogisticsItem extends BaseEntity {
    private Long logisticsId;
    private Long orderId;
    private Long orderItemId;
    private Integer qty;
}
