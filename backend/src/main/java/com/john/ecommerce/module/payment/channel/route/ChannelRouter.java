package com.john.ecommerce.module.payment.channel.route;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.john.ecommerce.common.exception.BizException;
import com.john.ecommerce.module.payment.channel.PayChannel;
import com.john.ecommerce.module.payment.entity.PayChannelConfig;
import com.john.ecommerce.module.payment.entity.PayRoutePolicy;
import com.john.ecommerce.module.payment.entity.PayRouteRule;
import com.john.ecommerce.module.payment.enums.RouteStrategyType;
import com.john.ecommerce.module.payment.mapper.PayChannelConfigMapper;
import com.john.ecommerce.module.payment.mapper.PayRoutePolicyMapper;
import com.john.ecommerce.module.payment.mapper.PayRouteRuleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
public class ChannelRouter {

    private final PayRouteRuleMapper routeRuleMapper;
    private final PayChannelConfigMapper channelConfigMapper;
    private final PayRoutePolicyMapper routePolicyMapper;
    private final PayChannelRegistry channelRegistry;

    public RouteResult route(String methodCode, String scene, Long payAccountId) {
        List<PayRouteRule> rules = routeRuleMapper.selectList(
                new LambdaQueryWrapper<PayRouteRule>()
                        .eq(PayRouteRule::getMethodCode, methodCode)
                        .eq(PayRouteRule::getScene, scene != null ? scene : "DEFAULT")
                        .eq(PayRouteRule::getStatus, 1)
                        .orderByAsc(PayRouteRule::getPriority));

        if (rules.isEmpty() && payAccountId != null) {
            return routeByAccount(payAccountId);
        }
        if (rules.isEmpty()) {
            throw new BizException("未配置支付路由规则");
        }

        PayRouteRule rule = rules.get(0);
        Long accountId = rule.getPayAccountId() != null ? rule.getPayAccountId() : payAccountId;
        String channelType = rule.getChannelType();

        return resolveConfig(accountId, channelType);
    }

    private RouteResult routeByAccount(Long payAccountId) {
        List<PayChannelConfig> configs = channelConfigMapper.selectList(
                new LambdaQueryWrapper<PayChannelConfig>()
                        .eq(PayChannelConfig::getPayAccountId, payAccountId)
                        .eq(PayChannelConfig::getStatus, 1)
                        .orderByDesc(PayChannelConfig::getWeight));
        if (configs.isEmpty()) {
            throw new BizException("支付账户无可用渠道");
        }

        PayRoutePolicy policy = routePolicyMapper.selectOne(
                new LambdaQueryWrapper<PayRoutePolicy>().eq(PayRoutePolicy::getStatus, 1).last("LIMIT 1"));
        RouteStrategyType strategy = policy != null ? RouteStrategyType.of(policy.getStrategyType()) : RouteStrategyType.PRIORITY;

        PayChannelConfig chosen = switch (strategy) {
            case WEIGHT_LB -> weightedSelect(configs);
            case FAIL_FAST -> configs.get(0);
            default -> configs.stream()
                    .max(Comparator.comparingInt(c -> c.getWeight() != null ? c.getWeight() : 0))
                    .orElse(configs.get(0));
        };

        PayChannel channel = channelRegistry.get(chosen.getChannelType());
        if (channel == null) {
            throw new BizException("渠道未实现: " + chosen.getChannelType());
        }
        RouteResult result = new RouteResult();
        result.setChannelConfig(chosen);
        result.setPayChannel(channel);
        return result;
    }

    private RouteResult resolveConfig(Long payAccountId, String channelType) {
        LambdaQueryWrapper<PayChannelConfig> w = new LambdaQueryWrapper<PayChannelConfig>()
                .eq(payAccountId != null, PayChannelConfig::getPayAccountId, payAccountId)
                .eq(channelType != null, PayChannelConfig::getChannelType, channelType)
                .eq(PayChannelConfig::getStatus, 1)
                .orderByDesc(PayChannelConfig::getWeight)
                .last("LIMIT 1");
        PayChannelConfig config = channelConfigMapper.selectOne(w);
        if (config == null) {
            throw new BizException("无可用渠道配置");
        }
        String type = channelType != null ? channelType : config.getChannelType();
        PayChannel channel = channelRegistry.get(type);
        if (channel == null) {
            throw new BizException("渠道未实现: " + type);
        }
        RouteResult result = new RouteResult();
        result.setChannelConfig(config);
        result.setPayChannel(channel);
        return result;
    }

    private PayChannelConfig weightedSelect(List<PayChannelConfig> configs) {
        int total = configs.stream().mapToInt(c -> c.getWeight() != null ? c.getWeight() : 1).sum();
        int rand = ThreadLocalRandom.current().nextInt(total);
        int cumulative = 0;
        for (PayChannelConfig c : configs) {
            cumulative += (c.getWeight() != null ? c.getWeight() : 1);
            if (rand < cumulative) return c;
        }
        return configs.get(configs.size() - 1);
    }
}
