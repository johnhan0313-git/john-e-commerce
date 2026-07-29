package com.john.ecommerce.module.payment.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.john.ecommerce.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_settlement_order_item")
public class SettlementOrderItem extends BaseEntity {
    private Long settlementOrderId;
    private String outAccountType;
    private Long outAccountId;
    private String inAccountType;
    private Long inAccountId;
    private Long amount;
    private String feeType;
    private String tradeType;
}
