package com.john.ecommerce.module-campaign.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.john.ecommerce.module-campaign.entity.Campaign;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface CampaignMapper extends BaseMapper<Campaign> {

    @Select("SELECT * FROM t_campaign WHERE tenant_id = #{tenantId} AND status = 2 " +
            "AND start_time <= #{now} AND end_time >= #{now} ORDER BY priority DESC")
    List<Campaign> selectActive(@Param("tenantId") Long tenantId, @Param("now") long now);
}
