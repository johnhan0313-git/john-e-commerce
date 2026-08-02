package com.john.ecommerce.module.tenant.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TenantBrandingUpdateDTO {
    @Size(max = 100, message = "展示名称过长")
    private String displayName;

    @Size(max = 512, message = "Logo URL 过长")
    private String logo;

    @Size(max = 512, message = "Favicon URL 过长")
    private String favicon;
}
