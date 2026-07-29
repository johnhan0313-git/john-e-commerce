package com.john.ecommerce.module.content.dto;

import lombok.Data;

import java.util.Map;

@Data
public class PageTemplateVO {
    private Long id;
    private String name;
    private String templateType;
    private Map<String, Object> config;
    private Integer status;
    private Long createdAt;
    private Long updatedAt;
}
