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

    // 3. 库存告警列表 (里面包含 食材名称 和 剩余总数量)
    private List<Map<String, Object>> lowStockList;
}
