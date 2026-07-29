package com.john.ecommerce.module.activity.service.handler;

import com.john.ecommerce.common.enums.ActivityType;
import com.john.ecommerce.module.activity.entity.Activity;
import com.john.ecommerce.module.activity.service.engine.PromoCandidate;
import com.john.ecommerce.module.activity.service.engine.PromoContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
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
    @SuppressWarnings("unchecked")
    public List<PromoCandidate> evaluate(Activity activity, PromoContext context) {
        Map<String, Object> rule = activity.getRuleConfig();
        if (rule == null) return List.of();
        BigDecimal threshold = toDecimal(rule.get("threshold"));
        BigDecimal reduction = toDecimal(rule.get("reduction"));
        if (threshold == null || reduction == null) return List.of();

        BigDecimal orderTotal = context.getLines().stream()
                .map(PromoContext.PromoLine::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (orderTotal.compareTo(threshold) < 0) return List.of();

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

    private BigDecimal toDecimal(Object v) {
        if (v == null) return null;
        if (v instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        return new BigDecimal(v.toString());
    }
}
