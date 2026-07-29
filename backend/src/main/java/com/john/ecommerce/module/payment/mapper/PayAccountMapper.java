package com.john.ecommerce.module.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.john.ecommerce.module.payment.entity.PayAccount;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PayAccountMapper extends BaseMapper<PayAccount> {
}
