package com.john.ecommerce.module.trade.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.john.ecommerce.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "t_order", autoResultMap = true)
public class Order extends BaseEntity {
    private String orderGroupNo;
    private String orderNo;
    private Long userId;
    private Long merchantId;
    private Long warehouseId;
    private Integer orderType;
    private Integer status;
    private String splitReason;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal payAmount;
    private BigDecimal paidAmount;
    private Integer payStatus;
    private Integer payType;
    private Long payTime;
    private String payNo;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private Integer diningType;
    private String tableNo;
    private String remark;
    private Long activityId;
    /** @deprecated use activityId — 库表已无 campaign_id 列 */
    @TableField(exist = false)
    private Long campaignId;
    private Long distributorId;
    private Long cancelTime;
    private String cancelReason;
    private Long cancelBy;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> extra;
}
