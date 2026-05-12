package com.student.scm.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.scm.dto.RequisitionOrderPageQueryDTO;
import com.student.scm.dto.RequisitionOrderSubmitDTO;
import com.student.scm.entity.ScmRequisitionOrder;
import com.student.scm.result.Result;
import com.student.scm.service.IScmRequisitionDetailService;
import com.student.scm.service.IScmRequisitionOrderService;
import com.student.scm.vo.ScmRequisitionDetailVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/requisition_order")
public class ScmRequisitionOrderController {

    private final IScmRequisitionOrderService requisitionOrderService;
    private final IScmRequisitionDetailService requisitionDetailService;

    public ScmRequisitionOrderController(IScmRequisitionOrderService requisitionOrderService, IScmRequisitionDetailService requisitionDetailService) {
        this.requisitionOrderService = requisitionOrderService;
        this.requisitionDetailService = requisitionDetailService;
    }

    @PostMapping("/submit")
    public Result<String> submitOrder(@RequestBody RequisitionOrderSubmitDTO dto) {
        try {
            requisitionOrderService.submitOrder(dto);
            return Result.success("门店请购单提交成功");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/page")
    public Result<Page<ScmRequisitionOrder>> page(RequisitionOrderPageQueryDTO queryDTO) {
        Page<ScmRequisitionOrder> pageInfo = requisitionOrderService.queryPageByCondition(queryDTO);
        return Result.success(pageInfo);
    }

    @GetMapping("/details/{orderId}")
    public Result<List<ScmRequisitionDetailVO>> details(@PathVariable Long orderId) {
        return Result.success(requisitionDetailService.listByOrderId(orderId));
    }

    @PostMapping("/auditOutbound/{id}")
    public Result<String> auditOutbound(@PathVariable Long id) {
        try {
            requisitionOrderService.auditOutbound(id);
            return Result.success("出库审核通过，库存已扣减");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/rejectOutbound/{id}")
    public Result<String> rejectOutbound(@PathVariable Long id) {
        try {
            requisitionOrderService.rejectOutbound(id);
            return Result.success("单据已被驳回");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/pay/{id}")
    public Result<String> payOrder(@PathVariable Long id) {
        try {
            requisitionOrderService.confirmPayment(id);
            return Result.success("内部财务结算成功");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}
