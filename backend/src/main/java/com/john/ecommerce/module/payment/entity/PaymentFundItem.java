package com.john.ecommerce.module.payment.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.john.ecommerce.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_payment_fund_item")
public class PaymentFundItem extends BaseEntity {
    private Long paymentId;
    private String fundType;
    private Long ledgerAccountId;
    private Long channelConfigId;
    private BigDecimal amount;
}
