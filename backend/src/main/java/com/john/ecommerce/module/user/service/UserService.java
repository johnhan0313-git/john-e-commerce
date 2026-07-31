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
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final String PORTAL_ADMIN = "admin";

    private final UserMapper userMapper;
    private final EmailCodeService emailCodeService;

    @Value("${app.jwt.secret:john-ecommerce-dev-jwt-secret-change-me-32b+}")
    private String jwtSecret;

    @Value("${app.jwt.expire-ms:604800000}")
    private long jwtExpireMs;

    @Value("${app.auth.default-tenant-id:1}")
    private long defaultTenantId;

    public void sendLoginCode(EmailCodeSendDTO dto) {
        String email = EmailCodeService.normalize(dto.getEmail());
        if (!StringUtils.hasText(email)) {
            throw new BizException("邮箱不能为空");
        }
        User user = userMapper.selectByEmail(email);
        if (user != null && user.getStatus() != null && user.getStatus() != 1) {
            throw new BizException("账号已被禁用");
        }
        // 管理后台仅允许已有账号发码；买家/卖家未注册也可发码，登录时自动开通
        if (isAdminPortal(dto.getPortal()) && user == null) {
            throw new BizException("该邮箱未注册");
        }
        emailCodeService.sendLoginCode(email);
    }

    public LoginVO login(LoginDTO dto, Long headerTenantId) {
        String email = EmailCodeService.normalize(dto.getEmail());
        // 先校验不消费：JWT 签发失败时验证码仍可重试，避免误报「已过期」
        emailCodeService.verify(email, dto.getCode());

        User user = userMapper.selectByEmail(email);
        if (user == null) {
            if (isAdminPortal(dto.getPortal())) {
                throw new BizException("该邮箱未注册");
            }
            user = registerPasswordless(email, headerTenantId);
        } else if (user.getStatus() != null && user.getStatus() != 1) {
            throw new BizException("账号已被禁用");
        } else if (isAdminPortal(dto.getPortal()) && (user.getUserType() == null || user.getUserType() != 1)) {
            throw new BizException("无管理后台权限");
        }

        String token = generateToken(user);
        emailCodeService.consume(email);
        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUser(toVO(user));
        return vo;
    }

    /**
     * 邮箱验证码通过后自动开通买家/卖家账号（无密码）。
     */
    private User registerPasswordless(String email, Long headerTenantId) {
        Long tenantId = headerTenantId != null ? headerTenantId : defaultTenantId;
        Long prev = TenantContext.getTenantId();
        TenantContext.setTenantId(tenantId);
        try {
            User existing = userMapper.selectByEmail(email);
            if (existing != null) {
                return existing;
            }
            User user = new User();
            user.setTenantId(tenantId);
            user.setEmail(email);
            user.setNickname(nicknameFromEmail(email));
            user.setUserType(0);
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
        user.setNickname(StringUtils.hasText(dto.getNickname())
                ? dto.getNickname()
                : nicknameFromEmail(email));
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
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        // HS256 要求密钥 ≥ 256 bit；过短会在校验验证码之后才抛 WeakKeyException
        if (keyBytes.length < 32) {
            throw new BizException(503, "服务端 JWT 密钥配置过短，请将 JWT_SECRET 设为至少 32 字节");
        }
        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("userId", user.getId())
                .claim("tenantId", user.getTenantId())
                .claim("userType", String.valueOf(user.getUserType()))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpireMs))
                .signWith(Keys.hmacShaKeyFor(keyBytes))
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

    private static boolean isAdminPortal(String portal) {
        return PORTAL_ADMIN.equalsIgnoreCase(portal);
    }

    private static String nicknameFromEmail(String email) {
        int at = email.indexOf('@');
        return at > 0 ? email.substring(0, at) : email;
    }
}
