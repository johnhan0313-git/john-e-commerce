package com.john.ecommerce.module.tenant.dto;

import jakarta.validation.constraints.Email;
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
    @NotBlank(message = "租户管理员邮箱不能为空")
    @Email(message = "管理员邮箱格式不正确")
    private String adminEmail;
    private List<String> businessTypes;
    private Map<String, Object> config;
}
