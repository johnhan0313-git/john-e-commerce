package com.john.ecommerce.module.payment.dto;

import lombok.Data;

import java.util.Map;

@Data
public class PayMethodVO {
    private Long id;
    private String methodCode;
    private String name;
    private String iconUrl;
    private Integer sortOrder;
    private Integer status;
    private Map<String, Object> extra;
}
