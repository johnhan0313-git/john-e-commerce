package com.john.ecommerce.module-trade.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.john.ecommerce.module-trade.entity.Order;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}
