package com.john.ecommerce.module-product.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.john.ecommerce.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "t_spu", autoResultMap = true)
public class Spu extends BaseEntity {
    private Long merchantId;
    private Long categoryId;
    private String name;
    private String subtitle;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> mainImages;
    private String detail;
    private Integer productType;
    private Integer status;
    private Integer sales;
}
