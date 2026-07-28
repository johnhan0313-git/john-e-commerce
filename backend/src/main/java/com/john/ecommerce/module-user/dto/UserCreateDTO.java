package com.john.ecommerce.module-user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserCreateDTO {
    @NotBlank(message = "手机号不能为空")
    private String phone;
    private String email;
    private String nickname;
    private String password;
}
