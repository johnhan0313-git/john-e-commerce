package com.john.ecommerce.module.activity.service.handler;

import com.john.ecommerce.module.activity.entity.Activity;
import com.john.ecommerce.module.activity.service.engine.PromoCandidate;
import com.john.ecommerce.module.activity.service.engine.PromoContext;

import java.util.List;

public interface ActivityTypeHandler {
    String activityType();

    List<PromoCandidate> evaluate(Activity activity, PromoContext context);
}
