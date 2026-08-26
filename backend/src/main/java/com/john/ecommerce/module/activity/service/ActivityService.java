package com.john.ecommerce.module.activity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.john.ecommerce.common.context.TenantContext;
import com.john.ecommerce.common.context.UserContext;
import com.john.ecommerce.common.enums.ActivityType;
import com.john.ecommerce.common.exception.BizException;
import com.john.ecommerce.module.activity.dto.ActivityCreateDTO;
import com.john.ecommerce.module.activity.dto.ActivityVO;
import com.john.ecommerce.module.activity.dto.PromoPreviewDTO;
import com.john.ecommerce.module.activity.entity.Activity;
import com.john.ecommerce.module.activity.entity.ActivityScope;
import com.john.ecommerce.module.activity.mapper.ActivityMapper;
import com.john.ecommerce.module.activity.mapper.ActivityScopeMapper;
import com.john.ecommerce.module.activity.service.engine.PromoContext;
import com.john.ecommerce.module.activity.service.engine.PromoEngine;
import com.john.ecommerce.module.activity.service.engine.PromoOrderResult;
import com.john.ecommerce.module.product.entity.Sku;
import com.john.ecommerce.module.product.entity.Spu;
import com.john.ecommerce.module.product.mapper.SkuMapper;
import com.john.ecommerce.module.product.mapper.SpuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityMapper activityMapper;
    private final ActivityScopeMapper scopeMapper;
    private final PromoEngine promoEngine;
    private final SkuMapper skuMapper;
    private final SpuMapper spuMapper;

    @Transactional
    public ActivityVO create(ActivityCreateDTO dto) {
        Activity activity = new Activity();
        activity.setName(dto.getName());
        activity.setActivityType(dto.getActivityType());
        activity.setTitle(dto.getTitle());
        activity.setSubtitle(dto.getSubtitle());
        activity.setStartTime(dto.getStartTime());
        activity.setEndTime(dto.getEndTime());
        activity.setWarmUpTime(dto.getWarmUpTime());
        activity.setPriority(dto.getPriority() != null ? dto.getPriority() : 0);
        activity.setRuleConfig(dto.getRuleConfig());
        activity.setBudget(dto.getBudget());
        activity.setTotalQuota(dto.getTotalQuota());
        activity.setUsedBudget(0L);
        activity.setUsedQuota(0);
        activity.setStackable(true);
        activity.setPromoStage("order");
        activity.setStatus(0);
        activityMapper.insert(activity);

        if (dto.getScopes() != null) {
            for (ActivityCreateDTO.ScopeDTO s : dto.getScopes()) {
                ActivityScope scope = new ActivityScope();
                scope.setActivityId(activity.getId());
                scope.setScopeType(s.getScopeType());
                scope.setSpuId(s.getSpuId());
                scope.setCategoryId(s.getCategoryId());
                scope.setSkuId(s.getSkuId());
                scope.setActivityPrice(s.getActivityPrice());
                scope.setExtraConfig(s.getExtraConfig());
                scopeMapper.insert(scope);
            }
        }
        return getById(activity.getId());
    }

    public ActivityVO getById(Long id) {
        Activity activity = require(id);
        List<ActivityScope> scopes = scopeMapper.selectList(new LambdaQueryWrapper<ActivityScope>()
                .eq(ActivityScope::getActivityId, id));
        return toVO(activity, scopes);
    }

    public Page<ActivityVO> list(int page, int size, Integer status) {
        Page<Activity> p = activityMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Activity>()
                        .eq(status != null, Activity::getStatus, status)
                        .orderByDesc(Activity::getPriority)
                        .orderByDesc(Activity::getCreatedAt));
        Page<ActivityVO> result = new Page<>();
        result.setTotal(p.getTotal());
        result.setCurrent(p.getCurrent());
        result.setSize(p.getSize());
        result.setRecords(p.getRecords().stream().map(a -> toVO(a, List.of())).toList());
        return result;
    }

    public void publish(Long id) {
        Activity activity = require(id);
        activity.setStatus(1);
        activityMapper.updateById(activity);
    }

    public void offline(Long id) {
        Activity activity = require(id);
        activity.setStatus(2);
        activityMapper.updateById(activity);
    }

    public void delete(Long id) {
        require(id);
        scopeMapper.delete(new LambdaQueryWrapper<ActivityScope>().eq(ActivityScope::getActivityId, id));
        activityMapper.deleteById(id);
    }

    public PromoOrderResult preview(PromoPreviewDTO dto) {
        PromoContext context = new PromoContext();
        context.setTenantId(TenantContext.getTenantId());
        context.setUserId(UserContext.getCurrentUserId());
        for (PromoPreviewDTO.LineDTO line : dto.getLines()) {
            Sku sku = skuMapper.selectById(line.getSkuId());
            if (sku == null) throw new BizException("SKU不存在: " + line.getSkuId());
            Spu spu = spuMapper.selectById(sku.getSpuId());
            if (spu == null) throw new BizException("商品不存在");
            PromoContext.PromoLine pl = new PromoContext.PromoLine();
            pl.setSkuId(sku.getId());
            pl.setSpuId(spu.getId());
            pl.setCategoryId(spu.getCategoryId());
            pl.setMerchantId(spu.getMerchantId());
            pl.setQuantity(line.getQuantity());
            pl.setUnitPrice(sku.getPrice());
            long unit = sku.getPrice() != null ? sku.getPrice() : 0L;
            pl.setLineTotal(unit * line.getQuantity());
            context.getLines().add(pl);
        }
        return promoEngine.preview(context);
    }

    private Activity require(Long id) {
        Activity activity = activityMapper.selectById(id);
        if (activity == null) throw new BizException("活动不存在");
        return activity;
    }

    private ActivityVO toVO(Activity a, List<ActivityScope> scopes) {
        ActivityVO vo = new ActivityVO();
        vo.setId(a.getId());
        vo.setName(a.getName());
        vo.setActivityType(a.getActivityType());
        vo.setActivityTypeLabel(resolveTypeLabel(a.getActivityType()));
        vo.setTitle(a.getTitle());
        vo.setSubtitle(a.getSubtitle());
        vo.setStartTime(a.getStartTime());
        vo.setEndTime(a.getEndTime());
        vo.setWarmUpTime(a.getWarmUpTime());
        vo.setStatus(a.getStatus());
        vo.setStatusLabel(statusLabel(a.getStatus()));
        vo.setPriority(a.getPriority());
        vo.setRuleConfig(a.getRuleConfig());
        vo.setBudget(a.getBudget());
        vo.setUsedBudget(a.getUsedBudget());
        vo.setTotalQuota(a.getTotalQuota());
        vo.setUsedQuota(a.getUsedQuota());
        vo.setCreatedAt(a.getCreatedAt());
        vo.setScopes(scopes.stream().map(s -> {
            ActivityVO.ScopeVO sv = new ActivityVO.ScopeVO();
            sv.setId(s.getId());
            sv.setScopeType(s.getScopeType());
            sv.setSpuId(s.getSpuId());
            sv.setCategoryId(s.getCategoryId());
            sv.setSkuId(s.getSkuId());
            sv.setActivityPrice(s.getActivityPrice());
            return sv;
        }).toList());
        return vo;
    }

    private String resolveTypeLabel(String type) {
        for (ActivityType t : ActivityType.values()) {
            if (t.getCode().equals(type)) return t.getLabel();
        }
        return type;
    }

    private String statusLabel(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 0 -> "草稿";
            case 1 -> "已发布";
            case 2 -> "已下线";
            default -> "未知";
        };
    }
}
