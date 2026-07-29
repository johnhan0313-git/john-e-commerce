package com.john.ecommerce.module.tenant.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.john.ecommerce.common.exception.BizException;
import com.john.ecommerce.module.tenant.dto.TenantCreateDTO;
import com.john.ecommerce.module.tenant.dto.TenantVO;
import com.john.ecommerce.module.tenant.entity.Tenant;
import com.john.ecommerce.module.tenant.mapper.TenantMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TenantService {

    private final TenantMapper tenantMapper;

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
        return toVO(tenant);
    }

    public TenantVO getById(Long id) {
        Tenant tenant = tenantMapper.selectById(id);
        if (tenant == null) throw new BizException("租户不存在");
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
}
