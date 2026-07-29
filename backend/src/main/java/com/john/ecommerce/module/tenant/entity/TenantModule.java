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
@TableName(value = "t_tenant_module", autoResultMap = true)
public class TenantModule extends BaseEntity {
    private Long tenantId;
    private String moduleCode;
    private Integer status;
    private Long expireAt;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> config;
}
