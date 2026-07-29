package com.john.ecommerce.module.tenant.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.john.ecommerce.common.context.TenantContext;
import com.john.ecommerce.common.exception.BizException;
import com.john.ecommerce.common.module.ModuleCodes;
import com.john.ecommerce.common.module.TenantModuleChecker;
import com.john.ecommerce.module.tenant.dto.ModuleDefVO;
import com.john.ecommerce.module.tenant.dto.TenantModuleEnableDTO;
import com.john.ecommerce.module.tenant.dto.TenantModuleVO;
import com.john.ecommerce.module.tenant.entity.ModuleDef;
import com.john.ecommerce.module.tenant.entity.TenantModule;
import com.john.ecommerce.module.tenant.mapper.ModuleDefMapper;
import com.john.ecommerce.module.tenant.mapper.TenantModuleMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Primary
@RequiredArgsConstructor
public class TenantModuleService implements TenantModuleChecker {

    private final ModuleDefMapper moduleDefMapper;
    private final TenantModuleMapper tenantModuleMapper;

    @PostConstruct
    public void seedModulesIfEmpty() {
        Long count = moduleDefMapper.selectCount(null);
        if (count != null && count > 0) return;
        seedModule(ModuleCodes.TENANT, "租户基础", 1, 0);
        seedModule(ModuleCodes.PRODUCT, "商品", 1, 10);
        seedModule(ModuleCodes.TRADE, "交易", 1, 20);
        seedModule(ModuleCodes.ACTIVITY, "营销", 0, 30);
        seedModule(ModuleCodes.PAYMENT, "支付", 0, 40);
        seedModule(ModuleCodes.LEDGER, "账本", 0, 50);
        seedModule(ModuleCodes.SETTLE, "结算", 0, 60);
        seedModule(ModuleCodes.FULFILLMENT, "履约", 0, 70);
        seedModule(ModuleCodes.PURCHASE, "采购", 0, 80);
        seedModule(ModuleCodes.MERCHANT, "商家", 0, 90);
        seedModule(ModuleCodes.CONTENT, "内容", 0, 100);
        seedModule(ModuleCodes.STATISTICS, "统计", 0, 110);
        seedModule(ModuleCodes.CROSSBORDER, "跨境", 0, 120);
    }

    private void seedModule(String code, String name, int defaultEnabled, int sort) {
        ModuleDef def = new ModuleDef();
        def.setModuleCode(code);
        def.setModuleName(name);
        def.setDefaultEnabled(defaultEnabled);
        def.setSortOrder(sort);
        def.setStatus(1);
        moduleDefMapper.insert(def);
    }

    @Override
    public boolean isEnabled(Long tenantId, String moduleCode) {
        if (tenantId == null) return false;
        if (ModuleCodes.TENANT.equals(moduleCode)) return true;
        TenantModule tm = tenantModuleMapper.selectOne(new LambdaQueryWrapper<TenantModule>()
                .eq(TenantModule::getTenantId, tenantId)
                .eq(TenantModule::getModuleCode, moduleCode));
        if (tm != null) {
            if (tm.getStatus() != null && tm.getStatus() == 1) {
                return tm.getExpireAt() == null || tm.getExpireAt() > System.currentTimeMillis();
            }
            return false;
        }
        ModuleDef def = moduleDefMapper.selectOne(new LambdaQueryWrapper<ModuleDef>()
                .eq(ModuleDef::getModuleCode, moduleCode));
        return def != null && Integer.valueOf(1).equals(def.getDefaultEnabled());
    }

    public List<ModuleDefVO> listModuleDefs() {
        return moduleDefMapper.selectList(new LambdaQueryWrapper<ModuleDef>()
                        .orderByAsc(ModuleDef::getSortOrder))
                .stream().map(this::toDefVO).toList();
    }

    public List<TenantModuleVO> listTenantModules(Long tenantId) {
        if (tenantId == null) tenantId = TenantContext.getTenantId();
        if (tenantId == null) throw new BizException("缺少租户上下文");
        Map<String, String> names = moduleDefMapper.selectList(null).stream()
                .collect(Collectors.toMap(ModuleDef::getModuleCode, ModuleDef::getModuleName, (a, b) -> a));
        return tenantModuleMapper.selectList(new LambdaQueryWrapper<TenantModule>()
                        .eq(TenantModule::getTenantId, tenantId))
                .stream().map(tm -> toModuleVO(tm, names.get(tm.getModuleCode()))).toList();
    }

    @Transactional
    public TenantModuleVO enable(Long tenantId, TenantModuleEnableDTO dto) {
        if (tenantId == null) tenantId = TenantContext.getTenantId();
        if (tenantId == null) throw new BizException("缺少租户上下文");
        ModuleDef def = moduleDefMapper.selectOne(new LambdaQueryWrapper<ModuleDef>()
                .eq(ModuleDef::getModuleCode, dto.getModuleCode()));
        if (def == null) throw new BizException("模块不存在");

        TenantModule tm = tenantModuleMapper.selectOne(new LambdaQueryWrapper<TenantModule>()
                .eq(TenantModule::getTenantId, tenantId)
                .eq(TenantModule::getModuleCode, dto.getModuleCode()));
        if (tm == null) {
            tm = new TenantModule();
            tm.setTenantId(tenantId);
            tm.setModuleCode(dto.getModuleCode());
        }
        tm.setStatus(1);
        tm.setExpireAt(dto.getExpireAt());
        tm.setConfig(dto.getConfig() != null ? dto.getConfig() : new LinkedHashMap<>());
        if (tm.getId() == null) {
            tenantModuleMapper.insert(tm);
        } else {
            tenantModuleMapper.updateById(tm);
        }
        return toModuleVO(tm, def.getModuleName());
    }

    public void disable(Long tenantId, String moduleCode) {
        if (tenantId == null) tenantId = TenantContext.getTenantId();
        TenantModule tm = tenantModuleMapper.selectOne(new LambdaQueryWrapper<TenantModule>()
                .eq(TenantModule::getTenantId, tenantId)
                .eq(TenantModule::getModuleCode, moduleCode));
        if (tm == null) throw new BizException("租户未开通该模块");
        tm.setStatus(0);
        tenantModuleMapper.updateById(tm);
    }

    private ModuleDefVO toDefVO(ModuleDef d) {
        ModuleDefVO vo = new ModuleDefVO();
        vo.setId(d.getId());
        vo.setModuleCode(d.getModuleCode());
        vo.setModuleName(d.getModuleName());
        vo.setDescription(d.getDescription());
        vo.setDependencies(d.getDependencies());
        vo.setDefaultEnabled(d.getDefaultEnabled());
        vo.setSortOrder(d.getSortOrder());
        vo.setStatus(d.getStatus());
        return vo;
    }

    private TenantModuleVO toModuleVO(TenantModule tm, String moduleName) {
        TenantModuleVO vo = new TenantModuleVO();
        vo.setId(tm.getId());
        vo.setTenantId(tm.getTenantId());
        vo.setModuleCode(tm.getModuleCode());
        vo.setModuleName(moduleName);
        vo.setStatus(tm.getStatus());
        vo.setExpireAt(tm.getExpireAt());
        vo.setConfig(tm.getConfig());
        return vo;
    }
}
