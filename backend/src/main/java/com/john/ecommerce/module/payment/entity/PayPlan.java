package com.john.ecommerce.module.payment.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.john.ecommerce.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_pay_plan")
public class PayPlan extends BaseEntity {
    private Long orderId;
    private String planNo;
    private BigDecimal amount;
    private Long dueAt;
    private Integer status;
    private Long paidPaymentId;
}
