package com.john.ecommerce.module.content.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.john.ecommerce.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "t_navigation", autoResultMap = true)
public class Navigation extends BaseEntity {
    private String name;
    private String iconUrl;
    private String linkUrl;
    private Integer sortOrder;
    private Long parentId;
    private Integer status;
}
