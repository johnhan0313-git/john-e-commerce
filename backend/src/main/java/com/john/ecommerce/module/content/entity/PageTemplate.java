package com.john.ecommerce.module.content.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.john.ecommerce.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "t_page_template", autoResultMap = true)
public class PageTemplate extends BaseEntity {
    private String name;
    private String templateType;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> config;
    private Integer status;
}
