package com.john.ecommerce.module-user.controller;

import com.john.ecommerce.common.result.R;
import com.john.ecommerce.module-user.dto.LoginDTO;
import com.john.ecommerce.module-user.dto.LoginVO;
import com.john.ecommerce.module-user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/login")
    public R<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return R.ok(userService.login(dto));
    }
}
