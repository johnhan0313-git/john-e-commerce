package com.john.ecommerce.module.payment.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.john.ecommerce.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_settlement_item")
public class SettlementItem extends BaseEntity {
    private Long settlementId;
    private String accountType;
    private Long accountId;
    private String direction;
    private Long amount;
}
