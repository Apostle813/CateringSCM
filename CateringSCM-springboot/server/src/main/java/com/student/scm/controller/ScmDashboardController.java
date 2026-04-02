package com.student.scm.controller;

import com.student.scm.result.Result;
import com.student.scm.service.IScmInventoryService;
import com.student.scm.service.IScmPurchaseOrderService;
import com.student.scm.vo.DashboardVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/dashboard")
@RestController
public class ScmDashboardController {
    private final IScmInventoryService inventoryService;
    private final IScmPurchaseOrderService purchaseOrderService;

    public ScmDashboardController(IScmInventoryService inventoryService, IScmPurchaseOrderService purchaseOrderService) {
        this.inventoryService = inventoryService;
        this.purchaseOrderService = purchaseOrderService;
    }
    @GetMapping("/stat")
    public Result<DashboardVO> getDashboardStat() {
        DashboardVO vo = new DashboardVO();

        // 1. 查总资产
        vo.setTotalInventoryAsset(inventoryService.getTotalInventoryAsset());
        // 2. 查告警列表
        vo.setLowStockList(inventoryService.getLowStockAlerts());
        // 3. 查本月支出
        vo.setMonthPurchaseAmount(purchaseOrderService.getMonthPurchaseAmount());

        return Result.success(vo);
    }
}
