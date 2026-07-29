package com.john.ecommerce.module.fulfillment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.john.ecommerce.module.fulfillment.entity.StockOrderItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StockOrderItemMapper extends BaseMapper<StockOrderItem> {
}
