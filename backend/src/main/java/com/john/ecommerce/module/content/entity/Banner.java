package com.john.ecommerce.module.content.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.john.ecommerce.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "t_banner", autoResultMap = true)
public class Banner extends BaseEntity {
    private String title;
    private String imageUrl;
    private String linkUrl;
    private String position;
    private Integer sortOrder;
    private Integer status;
    private Long startTime;
    private Long endTime;
}
