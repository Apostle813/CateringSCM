package com.student.scm.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.scm.dto.PurchaseOrderPageQueryDTO;
import com.student.scm.dto.PurchaseOrderRejectDTO;
import com.student.scm.dto.PurchaseOrderSubmitDTO;
import com.student.scm.dto.PurchaseQuickOrderDTO;
import com.student.scm.entity.ScmPurchaseOrder;
import com.student.scm.result.Result;
import com.student.scm.service.IScmPurchaseOrderService;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

@Data
@RestController
@RequestMapping("/purchase_order")
public class ScmPurchaseOrderController {
    private IScmPurchaseOrderService purchaseOrderService;

    public ScmPurchaseOrderController(IScmPurchaseOrderService purchaseOrderService) {
        this.purchaseOrderService = purchaseOrderService;
    }

    /**
     * 发起采购申请 (采购员操作)
     */
    @PostMapping("/submit")
    public Result<String> submitOrder(@RequestBody PurchaseOrderSubmitDTO dto) {
        try {
            purchaseOrderService.submitOrder(dto);

            return Result.success("采购申请提交成功");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 2. 分页条件查询采购单
     * 支持按单号模糊查询、按状态筛选2
     */
    @GetMapping("/page")
    public Result<Page<ScmPurchaseOrder>> page(PurchaseOrderPageQueryDTO queryDTO) {
        Page<ScmPurchaseOrder> pageInfo = purchaseOrderService.queryPageByCondition(queryDTO);
        return Result.success(pageInfo);
    }

    @PostMapping("/auditPass/{id}")
    public Result<String> auditPass(@PathVariable Long id) {
         purchaseOrderService.auditPass(id);
        return Result.success("审核通过，等待入库");
    }
    @PostMapping("/reject")
    public Result<String> rejectOrder(@RequestBody PurchaseOrderRejectDTO dto) {
        purchaseOrderService.rejectOrder(dto);
        return Result.success("采购单已成功驳回！");
    }
    @PostMapping("/pay/{id}")
    public Result payOrder(@PathVariable Long id) {
        purchaseOrderService.confirmPayment(id);
        return Result.success("财务结算成功");
    }
    @PostMapping("/inbound/{id}")
    public Result inboundOrder(@PathVariable Long id) {
        purchaseOrderService.executeInbound(id);
        return Result.success("采购入库成功，库存已增加");
    }
    @PostMapping("/quick")
    public Result quickPurchase(@RequestBody PurchaseQuickOrderDTO purchaseQuickOrderDTO) {
        purchaseOrderService.quickPurchase(purchaseQuickOrderDTO);
        return Result.success("采购入库成功，库存已增加");
    }


}
