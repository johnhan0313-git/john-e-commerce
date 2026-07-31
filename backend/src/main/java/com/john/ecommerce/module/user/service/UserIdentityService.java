package com.john.ecommerce.module.user.service;

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
        List<String> codes = userIdentityMapper.listActiveCodesByUserId(userId);
        return codes != null ? codes.stream().filter(StringUtils::hasText).distinct().toList() : List.of();
    }

    public boolean has(Long userId, String identityCode) {
        if (userId == null || !StringUtils.hasText(identityCode)) {
            return false;
        }
        return userIdentityMapper.countActive(userId, identityCode) > 0;
    }

    public void require(Long userId, String identityCode) {
        if (!has(userId, identityCode)) {
            throw new BizException(identityDeniedMessage(identityCode));
        }
    }

    /**
     * 幂等赋予身份；已停用则重新启用。
     *
     * @param tenantId 写入新行时使用；为空则取 {@link TenantContext}
     */
    public void ensure(Long userId, String identityCode, Long tenantId) {
        if (userId == null || !StringUtils.hasText(identityCode)) {
            throw new BizException("身份参数无效");
        }
        UserIdentity existing = userIdentityMapper.selectByUserAndCode(userId, identityCode);
        if (existing != null) {
            if (existing.getStatus() == null || existing.getStatus() != 1) {
                Long prev = TenantContext.getTenantId();
                TenantContext.setTenantId(existing.getTenantId());
                try {
                    existing.setStatus(1);
                    userIdentityMapper.updateById(existing);
                } finally {
                    if (prev == null) {
                        TenantContext.clear();
                    } else {
                        TenantContext.setTenantId(prev);
                    }
                }
            }
            return;
        }
        Long tid = tenantId != null ? tenantId : TenantContext.getTenantId();
        if (tid == null) {
            throw new BizException("缺少租户上下文");
        }
        Long prev = TenantContext.getTenantId();
        TenantContext.setTenantId(tid);
        try {
            // 并发下再查一次
            existing = userIdentityMapper.selectByUserAndCode(userId, identityCode);
            if (existing != null) {
                return;
            }
            UserIdentity row = new UserIdentity();
            row.setTenantId(tid);
            row.setUserId(userId);
            row.setIdentityCode(identityCode);
            row.setStatus(1);
            row.setDeleteFlag(0);
            userIdentityMapper.insert(row);
        } finally {
            if (prev == null) {
                TenantContext.clear();
            } else {
                TenantContext.setTenantId(prev);
            }
        }
    }

    public void ensure(Long userId, String identityCode) {
        ensure(userId, identityCode, null);
    }

    public void ensureBuyer(Long userId) {
        ensure(userId, IdentityCodes.BUYER);
    }

    public void ensureBuyer(Long userId, Long tenantId) {
        ensure(userId, IdentityCodes.BUYER, tenantId);
    }

    public void ensureSeller(Long userId) {
        ensure(userId, IdentityCodes.SELLER);
    }

    public void ensureSeller(Long userId, Long tenantId) {
        ensure(userId, IdentityCodes.SELLER, tenantId);
    }

    public void ensureOps(Long userId) {
        ensure(userId, IdentityCodes.OPS);
    }

    public void ensureOps(Long userId, Long tenantId) {
        ensure(userId, IdentityCodes.OPS, tenantId);
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
