package com.john.ecommerce.common.outbox.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.john.ecommerce.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "t_event_outbox", autoResultMap = true)
public class EventOutbox extends BaseEntity {

    private String eventType;
    private String aggregateType;
    private Long aggregateId;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> payload;

    /** 0=PENDING 1=PROCESSING 2=DONE 3=DEAD */
    private Integer status;
    private Long nextRetryAt;
    private Integer attemptCount;
    private String lastError;
}
