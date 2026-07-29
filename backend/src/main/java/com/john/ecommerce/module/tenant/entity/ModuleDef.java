package com.john.ecommerce.module.tenant.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.john.ecommerce.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "t_module_def", autoResultMap = true)
public class ModuleDef extends BaseEntity {
    private String moduleCode;
    private String moduleName;
    private String description;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> dependencies;
    private Integer defaultEnabled;
    private Integer sortOrder;
    private Integer status;
}
