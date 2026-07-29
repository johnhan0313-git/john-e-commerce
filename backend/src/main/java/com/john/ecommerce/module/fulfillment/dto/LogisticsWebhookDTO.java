package com.john.ecommerce.module.fulfillment.dto;

import lombok.Data;

@Data
public class LogisticsWebhookDTO {
    /** 0=in_transit 1=delivered */
    private Integer status;
    private Long eventTime;
    private String remark;
}
