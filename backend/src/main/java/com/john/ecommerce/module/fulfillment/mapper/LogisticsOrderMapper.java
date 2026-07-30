package com.john.ecommerce.module.fulfillment.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.john.ecommerce.module.fulfillment.entity.LogisticsOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface LogisticsOrderMapper extends BaseMapper<LogisticsOrder> {

    /** 物流回调无 JWT / 租户上下文，需按运单号跨租户定位。 */
    @InterceptorIgnore(tenantLine = "true")
    @Select("SELECT * FROM t_logistics_order WHERE tracking_no = #{trackingNo} AND delete_flag = 0 LIMIT 1")
    LogisticsOrder selectByTrackingNoIgnoreTenant(@Param("trackingNo") String trackingNo);
}
