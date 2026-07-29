package com.john.ecommerce.module.activity.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.john.ecommerce.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_activity_session")
public class ActivitySession extends BaseEntity {
    private Long activityId;
    private Long startTime;
    private Long endTime;
    private Integer stockLimit;
    private Integer soldQty;
    private Integer status;
}
