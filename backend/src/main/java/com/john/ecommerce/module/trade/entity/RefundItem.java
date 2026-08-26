package com.john.ecommerce.module.trade.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.john.ecommerce.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_refund_item")
public class RefundItem extends BaseEntity {
    private Long refundId;
    private Long orderItemId;
    private Long skuId;
    private Integer quantity;
    private Long amount;
    private Integer stockRestored;
}
