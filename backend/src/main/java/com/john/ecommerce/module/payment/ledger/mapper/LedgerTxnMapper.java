package com.john.ecommerce.module.payment.ledger.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.john.ecommerce.module.payment.ledger.entity.LedgerTxn;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LedgerTxnMapper extends BaseMapper<LedgerTxn> {
}
