package com.john.ecommerce.module.activity.service.handler;

import com.john.ecommerce.common.enums.ActivityType;
import com.john.ecommerce.module.activity.entity.Activity;
import com.john.ecommerce.module.activity.service.engine.PromoCandidate;
import com.john.ecommerce.module.activity.service.engine.PromoContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class FullReductionHandler implements ActivityTypeHandler {

    @Override
    public String activityType() {
        return ActivityType.FULL_REDUCTION.getCode();
    }

    @Override
    public List<PromoCandidate> evaluate(Activity activity, PromoContext context) {
        Map<String, Object> rule = activity.getRuleConfig();
        if (rule == null) return List.of();
        Long threshold = toLong(rule.get("threshold"));
        Long reduction = toLong(rule.get("reduction"));
        if (threshold == null || reduction == null) return List.of();

        long orderTotal = context.getLines().stream()
                .map(PromoContext.PromoLine::getLineTotal)
                .map(t -> t != null ? t : 0L)
                .reduce(0L, Long::sum);
        if (orderTotal < threshold) return List.of();

        PromoCandidate c = new PromoCandidate();
        c.setActivityId(activity.getId());
        c.setActivityType(activity.getActivityType());
        c.setStackGroup(activity.getStackGroup());
        c.setStackable(activity.getStackable());
        c.setPromoStage(activity.getPromoStage());
        c.setPriority(activity.getPriority());
        c.setDiscountAmount(reduction);
        c.setDescription("满减");
        return new ArrayList<>(List.of(c));
    }

    private Long toLong(Object v) {
        if (v == null) return null;
        if (v instanceof Number n) return n.longValue();
        return Long.parseLong(v.toString());
    }
}
