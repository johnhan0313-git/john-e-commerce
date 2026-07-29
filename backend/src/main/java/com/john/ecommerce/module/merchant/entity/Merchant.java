package com.john.ecommerce.module.merchant.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.john.ecommerce.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "t_merchant", autoResultMap = true)
public class Merchant extends BaseEntity {
    private Long userId;
    private String name;
    private String logo;
    private String licenseNo;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> licenseImages;
    private String contactName;
    private String contactPhone;
    private Integer status;
    private BigDecimal commissionRate;
    private Long settledAt;
}
