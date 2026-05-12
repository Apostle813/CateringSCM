package com.student.scm.controller;

import com.student.scm.mapper.ScmMaterialMapper;
import com.student.scm.mapper.ScmStockLogMapper;
import com.student.scm.mapper.ScmStoreMapper;
import com.student.scm.result.Result;
import com.student.scm.service.IScmInventoryService;
import com.student.scm.service.IScmPurchaseOrderService;
import com.student.scm.service.IScmRequisitionOrderService;
import com.student.scm.service.IScmSupplierService;
import com.student.scm.vo.DashboardVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/dashboard")
@RestController
public class ScmDashboardController {
    private final IScmInventoryService inventoryService;
    private final IScmPurchaseOrderService purchaseOrderService;
    private final IScmRequisitionOrderService requisitionOrderService;
    private final IScmSupplierService supplierService;
    private final ScmMaterialMapper materialMapper;
    private final ScmStoreMapper storeMapper;
    private final ScmStockLogMapper stockLogMapper;

    public ScmDashboardController(IScmInventoryService inventoryService,
                                  IScmPurchaseOrderService purchaseOrderService,
                                  IScmRequisitionOrderService requisitionOrderService,
                                  IScmSupplierService supplierService,
                                  ScmMaterialMapper materialMapper,
                                  ScmStoreMapper storeMapper,
                                  ScmStockLogMapper stockLogMapper) {
        this.inventoryService = inventoryService;
        this.purchaseOrderService = purchaseOrderService;
        this.requisitionOrderService = requisitionOrderService;
        this.supplierService = supplierService;
        this.materialMapper = materialMapper;
        this.storeMapper = storeMapper;
        this.stockLogMapper = stockLogMapper;
    }

    @GetMapping("/stat")
    public Result<DashboardVO> getDashboardStat() {
        DashboardVO vo = new DashboardVO();

        // 1. 总库存资产
        vo.setTotalInventoryAsset(inventoryService.getTotalInventoryAsset());
        // 2. 本月采购支出
        vo.setMonthPurchaseAmount(purchaseOrderService.getMonthPurchaseAmount());
        // 3. 库存告警
        vo.setLowStockList(inventoryService.getLowStockAlerts());

        // 4. 各仓库资产分布
        vo.setWarehouseAssets(inventoryService.getWarehouseAssetDistribution());
        // 5. 本周出入库统计
        vo.setWeeklyStockMovement(stockLogMapper.getWeeklyStockMovement());
        // 6. 近6月采购趋势
        vo.setPurchaseMonthlyTrend(purchaseOrderService.getMonthlyPurchaseTrend());

        // 7. 待审批采购单数
        vo.setPendingPurchaseCount(purchaseOrderService.countPending());
        // 8. 待审批请购单数
        vo.setPendingRequisitionCount(requisitionOrderService.countPending());
        // 9. 本月入库总量
        vo.setMonthInboundQty(inventoryService.getMonthInboundQty());
        // 10. 本月出库总量
        vo.setMonthOutboundQty(inventoryService.getMonthOutboundQty());

        // 11. 有效供应商数
        vo.setTotalSupplier(supplierService.countActive());
        // 12. 食材总数
        vo.setTotalMaterial(materialMapper.countAll());
        // 13. 门店总数
        vo.setTotalStore(storeMapper.selectCount(null));

        return Result.success(vo);
    }
}
