package com.john.ecommerce.module.payment.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.john.ecommerce.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_settlement_bill")
public class SettlementBill extends BaseEntity {
    private String billNo;
    private Long merchantId;
    private String payeeType;
    private Long payeeId;
    private Long periodStart;
    private Long periodEnd;
    private String currency;
    private Long billAmount;
    private Long preSettleAmount;
    private String status;
    private Integer settleStatus;
}
