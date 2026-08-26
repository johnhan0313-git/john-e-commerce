package com.john.ecommerce.common.outbox.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.john.ecommerce.common.outbox.entity.EventInbox;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EventInboxMapper extends BaseMapper<EventInbox> {
}
