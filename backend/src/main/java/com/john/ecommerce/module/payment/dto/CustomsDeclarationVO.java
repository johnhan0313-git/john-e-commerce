package com.john.ecommerce.module.payment.dto;

import lombok.Data;

import java.util.Map;

@Data
public class CustomsDeclarationVO {
    private Long id;
    private String declarationNo;
    private Long paymentId;
    private Long orderId;
    private String customsCode;
    private Integer status;
    private Long declaredAt;
    private String channelRefNo;
    private Map<String, Object> payload;
    private Long createdAt;
}
