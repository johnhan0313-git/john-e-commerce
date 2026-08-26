package com.john.ecommerce.module.payment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.john.ecommerce.common.exception.BizException;
import com.john.ecommerce.module.payment.entity.PayChannelConfig;
import com.john.ecommerce.module.payment.entity.PayRouteRule;
import com.john.ecommerce.module.payment.mapper.PayChannelConfigMapper;
import com.john.ecommerce.module.payment.mapper.PayRouteRuleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CashierRouter {

    private final PayRouteRuleMapper routeRuleMapper;
    private final PayChannelConfigMapper channelConfigMapper;

    public PayChannelConfig route(Long tenantId, String methodCode, String scene, Long amount) {
        List<PayRouteRule> rules = routeRuleMapper.selectList(new LambdaQueryWrapper<PayRouteRule>()
                .eq(PayRouteRule::getMethodCode, methodCode)
                .eq(PayRouteRule::getStatus, 1)
                .orderByDesc(PayRouteRule::getPriority));

        if (rules.isEmpty()) throw new BizException("未找到可用路由规则");

        PayRouteRule rule = rules.get(0);
        PayChannelConfig config = channelConfigMapper.selectOne(new LambdaQueryWrapper<PayChannelConfig>()
                .eq(PayChannelConfig::getPayAccountId, rule.getPayAccountId())
                .eq(PayChannelConfig::getChannelType, rule.getChannelType())
                .eq(PayChannelConfig::getStatus, 1)
                .last("LIMIT 1"));

        if (config == null) throw new BizException("未找到可用渠道配置");
        return config;
    }
}
