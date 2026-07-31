package com.john.ecommerce.module.user.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserVO {
    private Long id;
    private String phone;
    private String email;
    private String nickname;
    private String avatar;
    /** @deprecated 用 identities；1=有 ops，0=无 */
    private Integer userType;
    private List<String> identities;
    private Integer status;
    private Long createdAt;
}
