package com.john.ecommerce.module.fulfillment.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.john.ecommerce.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "t_supplier", autoResultMap = true)
public class Supplier extends BaseEntity {
    private String name;
    private String contactName;
    private String contactPhone;
    private String address;
    private Integer status;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> extra;
}
