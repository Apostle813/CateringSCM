package com.student.scm.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.scm.dto.PurchaseOrderPageQueryDTO;
import com.student.scm.dto.PurchaseOrderRejectDTO;
import com.student.scm.dto.PurchaseOrderSubmitDTO;
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

    @PostMapping("/audit/{id}")
    public Result<String> auditInbound(@PathVariable Long id) {
        // TODO: 待实现 Service 层的 auditInbound(id) 方法
         purchaseOrderService.auditInbound(id);
        return Result.success("审核通过，库存已增加");
    }
    @PostMapping("/reject")
    public Result<String> rejectOrder(@RequestBody PurchaseOrderRejectDTO dto) {
        purchaseOrderService.rejectOrder(dto);
        return Result.success("采购单已成功驳回！");
    }
}
