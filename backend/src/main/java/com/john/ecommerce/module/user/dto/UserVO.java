package com.john.ecommerce.module.user.dto;

import lombok.Data;

@Data
public class UserVO {
    private Long id;
    private String phone;
    private String email;
    private String nickname;
    private String avatar;
    private Integer userType;
    private Integer status;
    private Long createdAt;
}
