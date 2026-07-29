package com.john.ecommerce.module.product.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.john.ecommerce.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_cart")
public class Cart extends BaseEntity {
    private Long userId;
    private Long skuId;
    private Integer quantity;
    private Integer selected;
}
