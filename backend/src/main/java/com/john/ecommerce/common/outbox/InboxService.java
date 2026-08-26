package com.john.ecommerce.common.outbox;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.john.ecommerce.common.exception.BizException;
import com.john.ecommerce.common.outbox.entity.EventInbox;
import com.john.ecommerce.common.outbox.mapper.EventInboxMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Consumer-side deduplication for outbox handlers.
 * Runs the work then records the inbox row in the same transaction so failures roll back the claim.
 */
@Service
@RequiredArgsConstructor
public class InboxService {

    private final EventInboxMapper eventInboxMapper;

    /**
     * @return true if the runnable executed; false if this event was already processed
     */
    @Transactional
    public boolean tryProcess(Long eventId, Runnable runnable) {
        if (eventId == null) throw new BizException("事件ID不能为空");
        Long exists = eventInboxMapper.selectCount(new LambdaQueryWrapper<EventInbox>()
                .eq(EventInbox::getEventId, eventId));
        if (exists != null && exists > 0) {
            return false;
        }
        runnable.run();
        EventInbox inbox = new EventInbox();
        inbox.setEventId(eventId);
        inbox.setProcessedAt(System.currentTimeMillis());
        try {
            eventInboxMapper.insert(inbox);
        } catch (DuplicateKeyException e) {
            return false;
        }
        return true;
    }
}
