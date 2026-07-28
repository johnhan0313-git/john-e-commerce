package com.john.ecommerce.module-user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.john.ecommerce.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_user")
public class User extends BaseEntity {
    private String phone;
    private String email;
    private String nickname;
    private String avatar;
    private Integer userType;
    private Integer status;
    private String passwordHash;
    private String wxOpenid;
    private String wxUnionid;
}
