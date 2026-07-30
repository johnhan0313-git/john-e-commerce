package com.john.ecommerce.module.tenant.dto;

import lombok.Data;
import java.util.List;

@Data
public class ModuleDefVO {
    private Long id;
    private String moduleCode;
    private String moduleName;
    private String description;
    private List<String> dependencies;
    private Integer defaultEnabled;
    private Integer sortOrder;
    private Integer status;
}
