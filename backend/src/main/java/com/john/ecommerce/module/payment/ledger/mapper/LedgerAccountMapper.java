package com.john.ecommerce.module.payment.ledger.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.john.ecommerce.module.payment.ledger.entity.LedgerAccount;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LedgerAccountMapper extends BaseMapper<LedgerAccount> {
}
