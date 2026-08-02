package com.john.ecommerce.module.tenant.dto;

import lombok.Data;

@Data
public class TenantBrandingVO {
    private Long tenantId;
    private String name;
    private String slug;
    /** Mall 展示名，空则前端回退 name */
    private String displayName;
    private String logo;
    private String favicon;
}
