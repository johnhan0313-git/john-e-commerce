package com.john.ecommerce.module.user.dto;

import lombok.Data;

@Data
public class LoginVO {
    private String token;
    private UserVO user;
}
