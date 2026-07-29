package com.john.ecommerce.module.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.john.ecommerce.module.payment.entity.PayMethod;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PayMethodMapper extends BaseMapper<PayMethod> {
}
