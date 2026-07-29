package com.john.ecommerce.module.payment.ledger.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.john.ecommerce.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_ledger_txn")
public class LedgerTxn extends BaseEntity {
    private String txnNo;
    private String txnType;
    private Long amount;
    private String currency;
    private Integer status;
    private String bizType;
    private String refType;
    private Long refId;
    private String remark;
}
