package com.john.ecommerce.common.base;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;

@Data
public abstract class BaseEntity implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long tenantId;

    @TableField(fill = FieldFill.INSERT)
    private Long createdAt;

    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updatedAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updatedBy;

    private String idempotentKey;
}
