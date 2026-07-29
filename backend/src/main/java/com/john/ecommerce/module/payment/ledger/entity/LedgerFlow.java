package com.john.ecommerce.module.payment.ledger.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.john.ecommerce.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_ledger_flow")
public class LedgerFlow extends BaseEntity {
    private Long ledgerAccountId;
    private Long txnId;
    private String direction;
    private Long amount;
    private Long balanceBefore;
    private Long balanceAfter;
    private String bizType;
    private String refType;
    private Long refId;
    private String remark;
}
