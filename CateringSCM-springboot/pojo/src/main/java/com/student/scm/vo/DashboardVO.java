package com.student.scm.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class DashboardVO {
    // 1. 总库存资产 (元)
    private BigDecimal totalInventoryAsset;

    // 2. 本月采购支出 (元)
    private BigDecimal monthPurchaseAmount;

    // 3. 库存告警列表
    private List<Map<String, Object>> lowStockList;

    // ===== 新增字段 =====

    // 4. 各仓库资产分布 (饼图数据)
    private List<Map<String, Object>> warehouseAssets;

    // 5. 本周每日出入库统计 (柱状图数据)
    private List<Map<String, Object>> weeklyStockMovement;

    // 6. 近6月采购趋势 (折线图数据)
    private List<Map<String, Object>> purchaseMonthlyTrend;

    // 7. 待审批采购单数
    private Long pendingPurchaseCount;

    // 8. 待审批请购单数
    private Long pendingRequisitionCount;

    // 9. 本月入库总量
    private Integer monthInboundQty;

    // 10. 本月出库总量
    private Integer monthOutboundQty;

    // 11. 有效供应商数
    private Long totalSupplier;

    // 12. 食材总数
    private Long totalMaterial;

    // 13. 门店总数
    private Long totalStore;
}
