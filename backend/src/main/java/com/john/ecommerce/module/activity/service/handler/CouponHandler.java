package com.john.ecommerce.module.activity.service.handler;

import com.john.ecommerce.common.enums.ActivityType;
import com.john.ecommerce.module.activity.entity.Activity;
import com.john.ecommerce.module.activity.service.engine.PromoCandidate;
import com.john.ecommerce.module.activity.service.engine.PromoContext;
import org.springframework.stereotype.Component;

import java.util.List;

/** 优惠券占位：后续接 t_user_coupon */
@Component
public class CouponHandler implements ActivityTypeHandler {

    @Override
    public String activityType() {
        return ActivityType.COUPON.getCode();
    }

    @Override
    public List<PromoCandidate> evaluate(Activity activity, PromoContext context) {
        return List.of();
    }
}
