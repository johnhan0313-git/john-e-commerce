package com.john.ecommerce.module.payment.channel;

import com.john.ecommerce.module.payment.entity.SplitDetail;
import com.john.ecommerce.module.payment.entity.SplitOrder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class MockSplitChannel implements SplitChannel {

    @Override
    public boolean supports(String channelType) {
        return "MOCK".equalsIgnoreCase(channelType);
    }

    @Override
    public String createSplit(SplitOrder order, List<SplitDetail> details) {
        return "MOCK-SPLIT-" + UUID.randomUUID().toString().replace("-", "");
    }

    @Override
    public boolean confirmSplit(String channelSplitNo) {
        return true;
    }

    @Override
    public boolean modifySplit(String channelSplitNo, List<SplitDetail> details) {
        return true;
    }

    @Override
    public String querySplit(String channelSplitNo) {
        return "CONFIRMED";
    }
}
