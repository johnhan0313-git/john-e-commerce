package com.john.ecommerce.module.content.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

@Data
public class PageTemplateCreateDTO {
    @NotBlank(message = "模板名称不能为空")
    private String name;
    @NotBlank(message = "模板类型不能为空")
    private String templateType;
    private Map<String, Object> config;
    private Integer status;
}
