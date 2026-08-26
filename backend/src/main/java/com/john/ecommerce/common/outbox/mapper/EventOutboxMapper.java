package com.john.ecommerce.common.outbox.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.john.ecommerce.common.outbox.entity.EventOutbox;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EventOutboxMapper extends BaseMapper<EventOutbox> {
}
