package com.john.ecommerce.module.activity.service.handler;

import com.john.ecommerce.common.enums.ActivityType;
import com.john.ecommerce.module.activity.entity.Activity;
import com.john.ecommerce.module.activity.service.engine.PromoCandidate;
import com.john.ecommerce.module.activity.service.engine.PromoContext;
import org.springframework.stereotype.Component;

import java.util.List;

/** 拼团占位 */
@Component
public class GroupBuyHandler implements ActivityTypeHandler {

    @Override
    public String activityType() {
        return ActivityType.GROUP_BUY.getCode();
    }

    @Override
    public List<PromoCandidate> evaluate(Activity activity, PromoContext context) {
        return List.of();
    }
}
