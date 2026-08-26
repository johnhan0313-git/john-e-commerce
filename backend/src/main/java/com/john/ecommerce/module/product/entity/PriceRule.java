package com.john.ecommerce.module.product.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.john.ecommerce.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_price_rule")
public class PriceRule extends BaseEntity {
    private Long spuId;
    private Long skuId;
    private String ruleType;
    private Integer minQty;
    private Long price;
    private Long startTime;
    private Long endTime;
    private Integer status;
}
