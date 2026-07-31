package com.john.ecommerce.module.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.john.ecommerce.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_user_identity")
public class UserIdentity extends BaseEntity {
    private Long userId;
    /** buyer / seller / ops */
    private String identityCode;
    /** 1=启用 0=停用 */
    private Integer status;
}
