package com.john.ecommerce.module.tenant.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class TenantCreateDTO {
    @NotBlank(message = "租户名称不能为空")
    private String name;
    @NotBlank(message = "slug 不能为空")
    private String slug;
    private List<String> businessTypes;
    private Map<String, Object> config;
}
