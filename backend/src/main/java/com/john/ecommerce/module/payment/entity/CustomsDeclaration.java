package com.john.ecommerce.module.payment.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.john.ecommerce.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "t_customs_declaration", autoResultMap = true)
public class CustomsDeclaration extends BaseEntity {
    private String declarationNo;
    private Long paymentId;
    private Long orderId;
    private String customsCode;
    private Integer status;
    private Long declaredAt;
    private String channelRefNo;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> payload;
}
