package com.john.ecommerce.module.payment.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.john.ecommerce.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_settlement_bill_ref")
public class SettlementBillRef extends BaseEntity {
    private Long settlementBillId;
    private Long settlementOrderId;
}
