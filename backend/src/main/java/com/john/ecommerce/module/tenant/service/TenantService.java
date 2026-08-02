package com.john.ecommerce.module.tenant.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.john.ecommerce.common.context.TenantContext;
import com.john.ecommerce.common.exception.BizException;
import com.john.ecommerce.module.tenant.dto.TenantBrandingUpdateDTO;
import com.john.ecommerce.module.tenant.dto.TenantBrandingVO;
import com.john.ecommerce.module.tenant.dto.TenantCreateDTO;
import com.john.ecommerce.module.tenant.dto.TenantVO;
import com.john.ecommerce.module.tenant.entity.Tenant;
import com.john.ecommerce.module.tenant.mapper.TenantMapper;
import com.john.ecommerce.module.user.entity.User;
import com.john.ecommerce.module.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TenantService {

    public static final String CFG_LOGO = "logo";
    public static final String CFG_DISPLAY_NAME = "displayName";
    public static final String CFG_FAVICON = "favicon";

    private final TenantMapper tenantMapper;
    private final UserService userService;

    @Transactional
    public TenantVO create(TenantCreateDTO dto) {
        Tenant existing = tenantMapper.selectOne(new LambdaQueryWrapper<Tenant>().eq(Tenant::getSlug, dto.getSlug()));
        if (existing != null) throw new BizException("slug 已存在");
        Tenant tenant = new Tenant();
        tenant.setName(dto.getName());
        tenant.setSlug(dto.getSlug());
        tenant.setBusinessTypes(dto.getBusinessTypes() != null ? dto.getBusinessTypes() : Collections.emptyList());
        tenant.setConfig(dto.getConfig());
        tenant.setStatus(1);
        tenantMapper.insert(tenant);

        User admin = userService.createTenantAdmin(tenant.getId(), dto.getAdminEmail(), dto.getName() + "管理员");
        TenantVO vo = toVO(tenant);
        vo.setAdminEmail(admin.getEmail());
        vo.setAdminUserId(admin.getId());
        return vo;
    }

    public TenantVO getById(Long id) {
        Tenant tenant = requireTenant(id);
        return toVO(tenant);
    }

    public Page<TenantVO> list(int page, int size) {
        Page<Tenant> p = tenantMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Tenant>().orderByDesc(Tenant::getCreatedAt));
        Page<TenantVO> result = new Page<>();
        result.setTotal(p.getTotal());
        result.setCurrent(p.getCurrent());
        result.setSize(p.getSize());
        result.setRecords(p.getRecords().stream().map(this::toVO).toList());
        return result;
    }

    public TenantBrandingVO getBranding(Long tenantId) {
        return toBrandingVO(requireTenant(tenantId));
    }

    public TenantBrandingVO getCurrentBranding() {
        Long tid = TenantContext.getTenantId();
        if (tid == null) throw new BizException(400, "缺少租户上下文");
        return getBranding(tid);
    }

    @Transactional
    public TenantBrandingVO updateBranding(TenantBrandingUpdateDTO dto) {
        Long tid = TenantContext.getTenantId();
        if (tid == null) throw new BizException(400, "缺少租户上下文");
        Tenant tenant = requireTenant(tid);
        Map<String, Object> config = tenant.getConfig() != null
                ? new LinkedHashMap<>(tenant.getConfig())
                : new LinkedHashMap<>();
        putOrRemove(config, CFG_DISPLAY_NAME, trimToNull(dto.getDisplayName()));
        putOrRemove(config, CFG_LOGO, trimToNull(dto.getLogo()));
        putOrRemove(config, CFG_FAVICON, trimToNull(dto.getFavicon()));
        tenant.setConfig(config);
        tenantMapper.updateById(tenant);
        return toBrandingVO(tenant);
    }

    private Tenant requireTenant(Long id) {
        Tenant tenant = tenantMapper.selectById(id);
        if (tenant == null) throw new BizException("租户不存在");
        return tenant;
    }

    private TenantVO toVO(Tenant t) {
        TenantVO vo = new TenantVO();
        vo.setId(t.getId());
        vo.setName(t.getName());
        vo.setSlug(t.getSlug());
        vo.setBusinessTypes(t.getBusinessTypes());
        vo.setStatus(t.getStatus());
        vo.setConfig(t.getConfig());
        vo.setCreatedAt(t.getCreatedAt());
        return vo;
    }

    private TenantBrandingVO toBrandingVO(Tenant t) {
        TenantBrandingVO vo = new TenantBrandingVO();
        vo.setTenantId(t.getId());
        vo.setName(t.getName());
        vo.setSlug(t.getSlug());
        Map<String, Object> config = t.getConfig() != null ? t.getConfig() : Collections.emptyMap();
        vo.setDisplayName(configString(config, CFG_DISPLAY_NAME));
        vo.setLogo(configString(config, CFG_LOGO));
        vo.setFavicon(configString(config, CFG_FAVICON));
        return vo;
    }

    private static String configString(Map<String, Object> config, String key) {
        Object v = config.get(key);
        if (v == null) return null;
        String s = String.valueOf(v).trim();
        return s.isEmpty() || "null".equals(s) ? null : s;
    }

    private static String trimToNull(String s) {
        if (!StringUtils.hasText(s)) return null;
        return s.trim();
    }

    private static void putOrRemove(Map<String, Object> config, String key, String value) {
        if (value == null) config.remove(key);
        else config.put(key, value);
    }
}
