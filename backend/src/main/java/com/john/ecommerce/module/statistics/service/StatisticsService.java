package com.john.ecommerce.module.statistics.service;

import com.john.ecommerce.common.context.TenantContext;
import com.john.ecommerce.common.exception.BizException;
import com.john.ecommerce.module.statistics.dto.OverviewVO;
import com.john.ecommerce.module.statistics.mapper.StatisticsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final StatisticsMapper statisticsMapper;

    public OverviewVO overview() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) throw new BizException("缺少租户上下文");

        OverviewVO vo = new OverviewVO();

        // order count by status
        List<Map<String, Object>> statusRows = statisticsMapper.orderCountByStatus(tenantId);
        Map<Integer, Long> statusMap = new LinkedHashMap<>();
        for (Map<String, Object> row : statusRows) {
            Integer status = ((Number) row.get("status")).intValue();
            Long cnt = ((Number) row.get("cnt")).longValue();
            statusMap.put(status, cnt);
        }
        vo.setOrderCountByStatus(statusMap);

        // GMV
        BigDecimal gmv = statisticsMapper.gmvSum(tenantId);
        vo.setGmv(gmv != null ? gmv : BigDecimal.ZERO);

        // top skus
        List<Map<String, Object>> topRows = statisticsMapper.topSkusByQty(tenantId, 10);
        List<OverviewVO.TopSkuVO> topSkus = topRows.stream().map(row -> {
            OverviewVO.TopSkuVO s = new OverviewVO.TopSkuVO();
            s.setSkuId(((Number) row.get("sku_id")).longValue());
            s.setSkuName((String) row.get("sku_name"));
            s.setTotalQty(((Number) row.get("total_qty")).longValue());
            return s;
        }).toList();
        vo.setTopSkus(topSkus);

        // stock summary
        Map<String, Object> stockRow = statisticsMapper.stockSummary(tenantId);
        OverviewVO.StockSummaryVO stock = new OverviewVO.StockSummaryVO();
        if (stockRow != null) {
            stock.setSkuCount(((Number) stockRow.get("sku_count")).longValue());
            stock.setTotalAvailable(((Number) stockRow.get("total_available")).longValue());
            stock.setTotalLocked(((Number) stockRow.get("total_locked")).longValue());
        } else {
            stock.setSkuCount(0L);
            stock.setTotalAvailable(0L);
            stock.setTotalLocked(0L);
        }
        vo.setStockSummary(stock);

        return vo;
    }
}
