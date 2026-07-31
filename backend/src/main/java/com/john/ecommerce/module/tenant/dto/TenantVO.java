package com.john.ecommerce.module.tenant.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class TenantVO {
    private Long id;
    private String name;
    private String slug;
    private List<String> businessTypes;
    private Integer status;
    private Map<String, Object> config;
    private Long createdAt;
    /** 仅创建租户时回填 */
    private String adminEmail;
    private Long adminUserId;
}
