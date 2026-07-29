package com.john.ecommerce.module.payment.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.john.ecommerce.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_split_detail")
public class SplitDetail extends BaseEntity {
    private Long splitOrderId;
    private String receiverType;
    private Long receiverId;
    private String receiverAccount;
    private Long amount;
    private String description;
    private Integer status;
}
