package com.john.ecommerce.module.payment.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.john.ecommerce.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_settlement")
public class Settlement extends BaseEntity {
    private String settleNo;
    private Long settlementBillId;
    private Long merchantId;
    private Long shopId;
    private Long netAmount;
    private String currency;
    private Integer status;
    private Long settledAt;
}
