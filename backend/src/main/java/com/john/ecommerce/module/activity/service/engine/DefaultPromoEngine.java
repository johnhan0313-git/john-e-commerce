package com.john.ecommerce.module.activity.service.engine;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.john.ecommerce.module.activity.entity.Activity;
import com.john.ecommerce.module.activity.mapper.ActivityMapper;
import com.john.ecommerce.module.activity.service.handler.ActivityTypeHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DefaultPromoEngine implements PromoEngine {

    private final ActivityMapper activityMapper;
    private final StackingPolicy stackingPolicy;
    private final List<ActivityTypeHandler> handlers;

    @Override
    public PromoOrderResult preview(PromoContext context) {
        long now = System.currentTimeMillis();
        List<Activity> activities = activityMapper.selectList(new LambdaQueryWrapper<Activity>()
                .eq(Activity::getStatus, 1)
                .le(Activity::getStartTime, now)
                .ge(Activity::getEndTime, now)
                .orderByDesc(Activity::getPriority));

        Map<String, ActivityTypeHandler> handlerMap = handlers.stream()
                .collect(Collectors.toMap(ActivityTypeHandler::activityType, Function.identity(), (a, b) -> a));

        List<PromoCandidate> candidates = new ArrayList<>();
        for (Activity activity : activities) {
            ActivityTypeHandler handler = handlerMap.get(activity.getActivityType());
            if (handler != null) {
                candidates.addAll(handler.evaluate(activity, context));
            }
        }

        List<PromoCandidate> applied = stackingPolicy.select(candidates);
        BigDecimal orderDiscount = applied.stream()
                .map(PromoCandidate::getDiscountAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        PromoOrderResult result = new PromoOrderResult();
        BigDecimal total = BigDecimal.ZERO;
        for (PromoContext.PromoLine line : context.getLines()) {
            PromoOrderResult.PromoLineResult lr = new PromoOrderResult.PromoLineResult();
            lr.setSkuId(line.getSkuId());
            lr.setSpuId(line.getSpuId());
            lr.setQuantity(line.getQuantity());
            lr.setUnitPrice(line.getUnitPrice());
            lr.setLineTotal(line.getLineTotal());
            lr.setDiscountAmount(BigDecimal.ZERO);
            lr.setPayAmount(line.getLineTotal());
            result.getLines().add(lr);
            total = total.add(line.getLineTotal());
        }

        if (total.compareTo(BigDecimal.ZERO) > 0 && orderDiscount.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal remaining = orderDiscount;
            for (int i = 0; i < result.getLines().size(); i++) {
                PromoOrderResult.PromoLineResult lr = result.getLines().get(i);
                BigDecimal share;
                if (i == result.getLines().size() - 1) {
                    share = remaining;
                } else {
                    share = orderDiscount.multiply(lr.getLineTotal())
                            .divide(total, 2, RoundingMode.HALF_UP);
                    remaining = remaining.subtract(share);
                }
                lr.setDiscountAmount(share);
                lr.setPayAmount(lr.getLineTotal().subtract(share));
            }
        }

        result.setTotalAmount(total);
        result.setDiscountAmount(orderDiscount);
        result.setPayAmount(total.subtract(orderDiscount));
        result.setApplied(applied);
        return result;
    }
}
