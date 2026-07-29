package com.john.ecommerce.module.tenant.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.Map;

@Data
public class TenantModuleEnableDTO {
    @NotBlank(message = "模块编码不能为空")
    private String moduleCode;
    private Long expireAt;
    private Map<String, Object> config;
}
