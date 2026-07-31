package com.john.ecommerce.module.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.john.ecommerce.common.context.TenantContext;
import com.john.ecommerce.common.exception.BizException;
import com.john.ecommerce.module.user.entity.UserIdentity;
import com.john.ecommerce.module.user.identity.IdentityCodes;
import com.john.ecommerce.module.user.mapper.UserIdentityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserIdentityService {

    private final UserIdentityMapper userIdentityMapper;

    public List<String> listActiveCodes(Long userId) {
        if (userId == null) {
            return List.of();
        }
        return userIdentityMapper.selectList(new LambdaQueryWrapper<UserIdentity>()
                        .eq(UserIdentity::getUserId, userId)
                        .eq(UserIdentity::getStatus, 1)
                        .orderByAsc(UserIdentity::getIdentityCode))
                .stream()
                .map(UserIdentity::getIdentityCode)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
    }

    public boolean has(Long userId, String identityCode) {
        if (userId == null || !StringUtils.hasText(identityCode)) {
            return false;
        }
        return userIdentityMapper.selectCount(new LambdaQueryWrapper<UserIdentity>()
                .eq(UserIdentity::getUserId, userId)
                .eq(UserIdentity::getIdentityCode, identityCode)
                .eq(UserIdentity::getStatus, 1)) > 0;
    }

    public void require(Long userId, String identityCode) {
        if (!has(userId, identityCode)) {
            throw new BizException(identityDeniedMessage(identityCode));
        }
    }

    /**
     * 幂等赋予身份；已停用则重新启用。
     */
    public void ensure(Long userId, String identityCode) {
        if (userId == null || !StringUtils.hasText(identityCode)) {
            throw new BizException("身份参数无效");
        }
        UserIdentity existing = userIdentityMapper.selectOne(new LambdaQueryWrapper<UserIdentity>()
                .eq(UserIdentity::getUserId, userId)
                .eq(UserIdentity::getIdentityCode, identityCode)
                .last("LIMIT 1"));
        if (existing != null) {
            if (existing.getStatus() == null || existing.getStatus() != 1) {
                existing.setStatus(1);
                userIdentityMapper.updateById(existing);
            }
            return;
        }
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BizException("缺少租户上下文");
        }
        UserIdentity row = new UserIdentity();
        row.setTenantId(tenantId);
        row.setUserId(userId);
        row.setIdentityCode(identityCode);
        row.setStatus(1);
        row.setDeleteFlag(0);
        userIdentityMapper.insert(row);
    }

    public void ensureBuyer(Long userId) {
        ensure(userId, IdentityCodes.BUYER);
    }

    public void ensureSeller(Long userId) {
        ensure(userId, IdentityCodes.SELLER);
    }

    public void ensureOps(Long userId) {
        ensure(userId, IdentityCodes.OPS);
    }

    private static String identityDeniedMessage(String identityCode) {
        return switch (identityCode) {
            case IdentityCodes.OPS -> "无管理后台权限";
            case IdentityCodes.SELLER -> "无卖家身份";
            case IdentityCodes.BUYER -> "无买家身份";
            default -> "无对应身份权限";
        };
    }
}
