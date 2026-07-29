package com.john.ecommerce.module.content.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BannerCreateDTO {
    private String title;
    @NotBlank(message = "图片地址不能为空")
    private String imageUrl;
    private String linkUrl;
    private String position;
    private Integer sortOrder;
    private Integer status;
    private Long startTime;
    private Long endTime;
}
