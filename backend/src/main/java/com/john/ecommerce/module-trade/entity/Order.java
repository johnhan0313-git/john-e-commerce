package com.john.ecommerce.module-trade.entity;

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
    private String orderNo;
    private Long userId;
    private Long merchantId;
    private Integer orderType;
    private Integer status;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal payAmount;
    private Integer payType;
    private Long payTime;
    private String payNo;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private Integer diningType;
    private String tableNo;
    private String remark;
    private Long campaignId;
    private Long distributorId;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> extra;
}
