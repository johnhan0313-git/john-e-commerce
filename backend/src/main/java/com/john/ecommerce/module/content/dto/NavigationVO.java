package com.john.ecommerce.module.content.dto;

import lombok.Data;

import java.util.List;

@Data
public class NavigationVO {
    private Long id;
    private String name;
    private String iconUrl;
    private String linkUrl;
    private Integer sortOrder;
    private Long parentId;
    private Integer status;
    private Long createdAt;
    private List<NavigationVO> children;
}
