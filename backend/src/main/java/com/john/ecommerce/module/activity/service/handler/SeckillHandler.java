package com.john.ecommerce.module.activity.service.handler;

import com.john.ecommerce.common.enums.ActivityType;
import com.john.ecommerce.module.activity.entity.Activity;
import com.john.ecommerce.module.activity.entity.ActivityScope;
import com.john.ecommerce.module.activity.mapper.ActivityScopeMapper;
import com.john.ecommerce.module.activity.service.engine.PromoCandidate;
import com.john.ecommerce.module.activity.service.engine.PromoContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SeckillHandler implements ActivityTypeHandler {

    private final ActivityScopeMapper scopeMapper;

    @Override
    public String activityType() {
        return ActivityType.SECKILL.getCode();
    }

    @Override
    public List<PromoCandidate> evaluate(Activity activity, PromoContext context) {
        List<ActivityScope> scopes = scopeMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ActivityScope>()
                        .eq(ActivityScope::getActivityId, activity.getId()));
        Map<Long, Long> skuPrices = scopes.stream()
                .filter(s -> s.getSkuId() != null && s.getActivityPrice() != null)
                .collect(Collectors.toMap(ActivityScope::getSkuId, ActivityScope::getActivityPrice, (a, b) -> a));

        List<PromoCandidate> result = new ArrayList<>();
        for (PromoContext.PromoLine line : context.getLines()) {
            Long activityPrice = skuPrices.get(line.getSkuId());
            if (activityPrice == null) continue;
            long unit = line.getUnitPrice() != null ? line.getUnitPrice() : 0L;
            long diff = (unit - activityPrice) * line.getQuantity();
            if (diff <= 0) continue;
            PromoCandidate c = baseCandidate(activity);
            c.setDiscountAmount(diff);
            c.setDescription("秒杀价");
            result.add(c);
        }
        return result;
    }

    private PromoCandidate baseCandidate(Activity activity) {
        PromoCandidate c = new PromoCandidate();
        c.setActivityId(activity.getId());
        c.setActivityType(activity.getActivityType());
        c.setStackGroup(activity.getStackGroup());
        c.setStackable(activity.getStackable());
        c.setPromoStage(activity.getPromoStage());
        c.setPriority(activity.getPriority());
        return c;
    }
}
