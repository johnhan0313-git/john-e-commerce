package com.john.ecommerce.common.module;

import com.john.ecommerce.common.context.TenantContext;
import com.john.ecommerce.common.exception.BizException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class FeatureGateInterceptor implements HandlerInterceptor {

    private final TenantModuleChecker tenantModuleChecker;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod hm)) {
            return true;
        }
        RequiresModule ann = hm.getMethodAnnotation(RequiresModule.class);
        if (ann == null) {
            ann = hm.getBeanType().getAnnotation(RequiresModule.class);
        }
        if (ann == null) {
            return true;
        }
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BizException(403, "未登录或缺少租户上下文");
        }
        if (!tenantModuleChecker.isEnabled(tenantId, ann.value())) {
            throw new BizException(403, "租户未开通模块: " + ann.value());
        }
        return true;
    }
}
