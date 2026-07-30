package com.john.ecommerce.module.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.john.ecommerce.common.context.TenantContext;
import com.john.ecommerce.common.exception.BizException;
import com.john.ecommerce.module.user.dto.*;
import com.john.ecommerce.module.user.entity.User;
import com.john.ecommerce.module.user.mapper.UserMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.jwt.secret:change-me-in-production}")
    private String jwtSecret;

    @Value("${app.jwt.expire-ms:604800000}")
    private long jwtExpireMs;

    public LoginVO login(LoginDTO dto) {
        User user = userMapper.selectByPhone(dto.getPhone());
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
            throw new BizException("手机号或密码错误");
        }
        if (user.getStatus() != 1) {
            throw new BizException("账号已被禁用");
        }
        String token = generateToken(user);
        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUser(toVO(user));
        return vo;
    }

    public UserVO create(UserCreateDTO dto) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BizException("缺少租户上下文");
        }
        User existing = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getPhone, dto.getPhone()));
        if (existing != null) throw new BizException("手机号已注册");
        User user = new User();
        user.setTenantId(tenantId);
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setNickname(dto.getNickname());
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setUserType(0);
        user.setStatus(1);
        userMapper.insert(user);
        return toVO(user);
    }

    public UserVO getById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) throw new BizException("用户不存在");
        return toVO(user);
    }

    public Page<UserVO> list(int page, int size) {
        Page<User> p = userMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<User>().orderByDesc(User::getCreatedAt));
        Page<UserVO> result = new Page<>();
        result.setTotal(p.getTotal());
        result.setCurrent(p.getCurrent());
        result.setSize(p.getSize());
        result.setRecords(p.getRecords().stream().map(this::toVO).toList());
        return result;
    }

    private String generateToken(User user) {
        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("userId", user.getId())
                .claim("tenantId", user.getTenantId())
                .claim("userType", String.valueOf(user.getUserType()))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpireMs))
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    private UserVO toVO(User u) {
        UserVO vo = new UserVO();
        vo.setId(u.getId());
        vo.setPhone(u.getPhone());
        vo.setEmail(u.getEmail());
        vo.setNickname(u.getNickname());
        vo.setAvatar(u.getAvatar());
        vo.setUserType(u.getUserType());
        vo.setStatus(u.getStatus());
        vo.setCreatedAt(u.getCreatedAt());
        return vo;
    }
}
