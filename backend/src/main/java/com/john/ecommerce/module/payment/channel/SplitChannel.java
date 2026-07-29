package com.john.ecommerce.module.payment.channel;

import com.john.ecommerce.module.payment.entity.SplitOrder;
import com.john.ecommerce.module.payment.entity.SplitDetail;
import java.util.List;

public interface SplitChannel {
    boolean supports(String channelType);
    String createSplit(SplitOrder order, List<SplitDetail> details);
    boolean confirmSplit(String channelSplitNo);
    boolean modifySplit(String channelSplitNo, List<SplitDetail> details);
    String querySplit(String channelSplitNo);
}
