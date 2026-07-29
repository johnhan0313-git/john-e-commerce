package com.john.ecommerce.module.content.dto;

import lombok.Data;

@Data
public class BannerVO {
    private Long id;
    private String title;
    private String imageUrl;
    private String linkUrl;
    private String position;
    private Integer sortOrder;
    private Integer status;
    private Long startTime;
    private Long endTime;
    private Long createdAt;
}
