package com.john.ecommerce.module.payment.ledger.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.john.ecommerce.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_ledger_account")
public class LedgerAccount extends BaseEntity {
    private String ownerType;
    private Long ownerId;
    private String accountType;
    private String currency;
    private Long balance;
    private Long frozen;
    private Long available;
    @Version
    private Integer version;
    private Long channelConfigId;
    private Integer status;
}
