package com.john.ecommerce.module.trade.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.john.ecommerce.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_refund")
public class Refund extends BaseEntity {
    private String refundNo;
    private Long orderId;
    private Long paymentId;
    private Long userId;
    private BigDecimal refundAmount;
    private String reason;
    private Integer status;
    private Long approvedAt;
    private Long completedAt;
}
