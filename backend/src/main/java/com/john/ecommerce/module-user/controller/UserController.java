package com.john.ecommerce.module-user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.john.ecommerce.common.result.R;
import com.john.ecommerce.module-user.dto.UserCreateDTO;
import com.john.ecommerce.module-user.dto.UserVO;
import com.john.ecommerce.module-user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public R<UserVO> create(@Valid @RequestBody UserCreateDTO dto) {
        return R.ok(userService.create(dto));
    }

    @GetMapping("/{id}")
    public R<UserVO> getById(@PathVariable Long id) {
        return R.ok(userService.getById(id));
    }

    @GetMapping
    public R<Page<UserVO>> list(@RequestParam(defaultValue = "1") int page,
                                @RequestParam(defaultValue = "20") int size) {
        return R.ok(userService.list(page, size));
    }
}
