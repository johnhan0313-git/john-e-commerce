package com.john.ecommerce.module.fulfillment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.john.ecommerce.module.fulfillment.entity.PurchaseOrderItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PurchaseOrderItemMapper extends BaseMapper<PurchaseOrderItem> {
}
