package com.john.ecommerce.common.outbox.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.john.ecommerce.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_event_inbox")
public class EventInbox extends BaseEntity {

    /** Outbox row id (unique consumer key). */
    private Long eventId;
    private Long processedAt;
}
