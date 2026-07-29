package com.john.ecommerce.module.fulfillment.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.john.ecommerce.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_purchase_order")
public class PurchaseOrder extends BaseEntity {
    private String poNo;
    private Long supplierId;
    private Long warehouseId;
    private Long refActivityId;
    private String status;
    private BigDecimal totalAmount;
    private String remark;
    private Long approvedAt;
    private Long finishedAt;
}
