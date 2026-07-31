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
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final EmailCodeService emailCodeService;

    @Value("${app.jwt.secret:john-ecommerce-dev-jwt-secret-change-me-32b+}")
    private String jwtSecret;

    @Value("${app.jwt.expire-ms:604800000}")
    private long jwtExpireMs;

    public void sendLoginCode(EmailCodeSendDTO dto) {
        String email = EmailCodeService.normalize(dto.getEmail());
        User user = userMapper.selectByEmail(email);
        if (user == null) {
            throw new BizException("该邮箱未注册");
        }
        if (user.getStatus() != 1) {
            throw new BizException("账号已被禁用");
        }
        emailCodeService.sendLoginCode(email);
    }

    public LoginVO login(LoginDTO dto) {
        String email = EmailCodeService.normalize(dto.getEmail());
        emailCodeService.verifyAndConsume(email, dto.getCode());

        User user = userMapper.selectByEmail(email);
        if (user == null) {
            throw new BizException("该邮箱未注册");
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
        if (!StringUtils.hasText(dto.getEmail())) {
            throw new BizException("邮箱不能为空");
        }
        String email = EmailCodeService.normalize(dto.getEmail());
        if (userMapper.selectByEmail(email) != null) {
            throw new BizException("邮箱已注册");
        }
        if (StringUtils.hasText(dto.getPhone())) {
            User byPhone = userMapper.selectOne(
                    new LambdaQueryWrapper<User>().eq(User::getPhone, dto.getPhone()));
            if (byPhone != null) throw new BizException("手机号已注册");
        }
        User user = new User();
        user.setTenantId(tenantId);
        user.setPhone(dto.getPhone());
        user.setEmail(email);
        user.setNickname(dto.getNickname());
        if (StringUtils.hasText(dto.getPassword())) {
            user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        }
        user.setUserType(0);
        user.setStatus(1);
        userMapper.insert(user);
        return toVO(user);
    }

    /**
     * 创建租户时写入租户管理员（邮箱登录），需在对应租户上下文中调用。
     */
    public User createTenantAdmin(Long tenantId, String email, String nickname) {
        String normalized = EmailCodeService.normalize(email);
        if (userMapper.selectByEmail(normalized) != null) {
            throw new BizException("管理员邮箱已被占用");
        }
        Long prev = TenantContext.getTenantId();
        TenantContext.setTenantId(tenantId);
        try {
            User user = new User();
            user.setTenantId(tenantId);
            user.setEmail(normalized);
            user.setNickname(StringUtils.hasText(nickname) ? nickname : "租户管理员");
            user.setUserType(1);
            user.setStatus(1);
            user.setDeleteFlag(0);
            userMapper.insert(user);
            return user;
        } finally {
            if (prev == null) {
                TenantContext.clear();
            } else {
                TenantContext.setTenantId(prev);
            }
        }
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
