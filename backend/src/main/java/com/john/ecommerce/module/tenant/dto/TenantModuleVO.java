package com.john.ecommerce.module.tenant.dto;

import lombok.Data;
import java.util.Map;

@Data
public class TenantModuleVO {
    private Long id;
    private Long tenantId;
    private String moduleCode;
    private String moduleName;
    private Integer status;
    private Long expireAt;
    private Map<String, Object> config;
}
