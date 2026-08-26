package com.john.ecommerce.module.statistics.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface StatisticsMapper {

    @Select("SELECT status, COUNT(*) AS cnt FROM t_order WHERE tenant_id = #{tenantId} AND delete_flag = 0 GROUP BY status")
    List<Map<String, Object>> orderCountByStatus(@Param("tenantId") Long tenantId);

    @Select("SELECT COALESCE(SUM(pay_amount), 0) FROM t_order WHERE tenant_id = #{tenantId} AND delete_flag = 0 AND status != 5")
    Long gmvSum(@Param("tenantId") Long tenantId);

    @Select("SELECT sku_id, sku_name, SUM(quantity) AS total_qty FROM t_order_item WHERE tenant_id = #{tenantId} AND delete_flag = 0 GROUP BY sku_id, sku_name ORDER BY total_qty DESC LIMIT #{limit}")
    List<Map<String, Object>> topSkusByQty(@Param("tenantId") Long tenantId, @Param("limit") int limit);

    @Select("SELECT COUNT(DISTINCT sku_id) AS sku_count, COALESCE(SUM(available), 0) AS total_available, COALESCE(SUM(locked), 0) AS total_locked FROM t_warehouse_stock WHERE tenant_id = #{tenantId} AND delete_flag = 0")
    Map<String, Object> stockSummary(@Param("tenantId") Long tenantId);
}
