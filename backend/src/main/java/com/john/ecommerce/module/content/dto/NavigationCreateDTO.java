package com.john.ecommerce.module.content.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NavigationCreateDTO {
    @NotBlank(message = "导航名称不能为空")
    private String name;
    private String iconUrl;
    private String linkUrl;
    private Integer sortOrder;
    private Long parentId;
    private Integer status;
}
