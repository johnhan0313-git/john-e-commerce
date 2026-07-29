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
@TableName(value = "t_tenant_config", autoResultMap = true)
public class TenantConfig extends BaseEntity {
    private Long tenantId;
    private String configKey;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> configValue;
}
