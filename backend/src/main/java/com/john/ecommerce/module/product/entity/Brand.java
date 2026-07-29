package com.john.ecommerce.module.product.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.john.ecommerce.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_brand")
public class Brand extends BaseEntity {
    private String name;
    private String logo;
    private String description;
    private Integer sortOrder;
    private Integer status;
}
