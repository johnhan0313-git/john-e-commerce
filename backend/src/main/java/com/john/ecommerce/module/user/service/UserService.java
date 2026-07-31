package com.john.ecommerce.module.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.john.ecommerce.common.context.TenantContext;
import com.john.ecommerce.common.exception.BizException;
import com.john.ecommerce.module.user.dto.*;
import com.john.ecommerce.module.user.entity.User;
import com.john.ecommerce.module.user.identity.IdentityCodes;
import com.john.ecommerce.module.user.mapper.UserMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final String PORTAL_ADMIN = "admin";
    private static final String PORTAL_MERCHANT = "merchant";
    private static final String PORTAL_MALL = "mall";

    private final UserMapper userMapper;
    private final EmailCodeService emailCodeService;
    private final UserIdentityService userIdentityService;

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
        if (isAdminPortal(dto.getPortal())) {
            if (user == null || !userIdentityService.has(user.getId(), IdentityCodes.OPS)) {
                throw new BizException(user == null ? "该邮箱未注册" : "无管理后台权限");
            }
        }
        emailCodeService.sendLoginCode(email);
    }

    public LoginVO login(LoginDTO dto, Long headerTenantId) {
        String email = EmailCodeService.normalize(dto.getEmail());
        emailCodeService.verify(email, dto.getCode());

        String portal = normalizePortal(dto.getPortal());
        User user = userMapper.selectByEmail(email);
        if (user == null) {
            if (PORTAL_ADMIN.equals(portal)) {
                throw new BizException("该邮箱未注册");
            }
            user = registerPasswordless(email, headerTenantId, portal);
        } else if (user.getStatus() != null && user.getStatus() != 1) {
            throw new BizException("账号已被禁用");
        } else {
            ensurePortalIdentities(user, portal);
        }

        List<String> identities = userIdentityService.listActiveCodes(user.getId());
        if (PORTAL_ADMIN.equals(portal) && !identities.contains(IdentityCodes.OPS)) {
            throw new BizException("无管理后台权限");
        }

        String token = generateToken(user, identities);
        emailCodeService.consume(email);
        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUser(toVO(user, identities));
        return vo;
    }

    /**
     * 邮箱验证码通过后自动开通账号，并按门户赋予身份。
     */
    private User registerPasswordless(String email, Long headerTenantId, String portal) {
        Long tenantId = headerTenantId != null ? headerTenantId : defaultTenantId;
        Long prev = TenantContext.getTenantId();
        TenantContext.setTenantId(tenantId);
        try {
            User existing = userMapper.selectByEmail(email);
            if (existing != null) {
                ensurePortalIdentities(existing, portal);
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
            ensurePortalIdentities(user, portal);
            return user;
        } finally {
            if (prev == null) {
                TenantContext.clear();
            } else {
                TenantContext.setTenantId(prev);
            }
        }
    }

    /**
     * mall → buyer；merchant → buyer+seller；admin 不自动赋权。
     */
    private void ensurePortalIdentities(User user, String portal) {
        if (PORTAL_ADMIN.equals(portal)) {
            return;
        }
        Long tenantId = user.getTenantId();
        userIdentityService.ensureBuyer(user.getId(), tenantId);
        if (PORTAL_MERCHANT.equals(portal)) {
            userIdentityService.ensureSeller(user.getId(), tenantId);
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
        userIdentityService.ensureBuyer(user.getId(), tenantId);
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
            userIdentityService.ensureBuyer(user.getId(), tenantId);
            userIdentityService.ensureOps(user.getId(), tenantId);
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

    private String generateToken(User user, List<String> identities) {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new BizException(503, "服务端 JWT 密钥配置过短，请将 JWT_SECRET 设为至少 32 字节");
        }
        boolean ops = identities != null && identities.contains(IdentityCodes.OPS);
        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("userId", user.getId())
                .claim("tenantId", user.getTenantId())
                .claim("userType", ops ? "1" : "0")
                .claim("identities", identities != null ? identities : List.of())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpireMs))
                .signWith(Keys.hmacShaKeyFor(keyBytes))
                .compact();
    }

    private UserVO toVO(User u) {
        return toVO(u, userIdentityService.listActiveCodes(u.getId()));
    }

    private UserVO toVO(User u, List<String> identities) {
        UserVO vo = new UserVO();
        vo.setId(u.getId());
        vo.setPhone(u.getPhone());
        vo.setEmail(u.getEmail());
        vo.setNickname(u.getNickname());
        vo.setAvatar(u.getAvatar());
        boolean ops = identities != null && identities.contains(IdentityCodes.OPS);
        vo.setUserType(ops ? 1 : 0);
        vo.setIdentities(identities != null ? identities : List.of());
        vo.setStatus(u.getStatus());
        vo.setCreatedAt(u.getCreatedAt());
        return vo;
    }

    private static boolean isAdminPortal(String portal) {
        return PORTAL_ADMIN.equals(normalizePortal(portal));
    }

    private static String normalizePortal(String portal) {
        if (!StringUtils.hasText(portal)) {
            return PORTAL_MALL;
        }
        return portal.trim().toLowerCase();
    }

    private static String nicknameFromEmail(String email) {
        int at = email.indexOf('@');
        return at > 0 ? email.substring(0, at) : email;
    }
}
