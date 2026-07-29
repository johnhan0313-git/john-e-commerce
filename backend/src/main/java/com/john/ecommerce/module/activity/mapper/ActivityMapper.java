package com.john.ecommerce.module.activity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.john.ecommerce.module.activity.entity.Activity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface ActivityMapper extends BaseMapper<Activity> {

    @Select("SELECT * FROM t_activity WHERE tenant_id = #{tenantId} AND status = 2 " +
            "AND start_time <= #{now} AND end_time >= #{now} ORDER BY priority DESC")
    List<Activity> selectActive(@Param("tenantId") Long tenantId, @Param("now") long now);
}
