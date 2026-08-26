package com.john.ecommerce.module.activity.service.engine;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.john.ecommerce.module.activity.entity.Activity;
import com.john.ecommerce.module.activity.mapper.ActivityMapper;
import com.john.ecommerce.module.activity.service.handler.ActivityTypeHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
        long orderDiscount = applied.stream()
                .map(PromoCandidate::getDiscountAmount)
                .map(d -> d != null ? d : 0L)
                .reduce(0L, Long::sum);

        PromoOrderResult result = new PromoOrderResult();
        long total = 0L;
        for (PromoContext.PromoLine line : context.getLines()) {
            PromoOrderResult.PromoLineResult lr = new PromoOrderResult.PromoLineResult();
            lr.setSkuId(line.getSkuId());
            lr.setSpuId(line.getSpuId());
            lr.setQuantity(line.getQuantity());
            lr.setUnitPrice(line.getUnitPrice());
            lr.setLineTotal(line.getLineTotal());
            lr.setDiscountAmount(0L);
            lr.setPayAmount(line.getLineTotal());
            result.getLines().add(lr);
            total += line.getLineTotal() != null ? line.getLineTotal() : 0L;
        }

        if (total > 0 && orderDiscount > 0) {
            long remaining = orderDiscount;
            for (int i = 0; i < result.getLines().size(); i++) {
                PromoOrderResult.PromoLineResult lr = result.getLines().get(i);
                long lineTotal = lr.getLineTotal() != null ? lr.getLineTotal() : 0L;
                long share;
                if (i == result.getLines().size() - 1) {
                    share = remaining;
                } else {
                    share = orderDiscount * lineTotal / total;
                    remaining -= share;
                }
                lr.setDiscountAmount(share);
                lr.setPayAmount(lineTotal - share);
            }
        }

        result.setTotalAmount(total);
        result.setDiscountAmount(orderDiscount);
        result.setPayAmount(total - orderDiscount);
        result.setApplied(applied);
        return result;
    }
}
