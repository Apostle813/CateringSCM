package com.student.scm.controller;


import com.student.scm.result.Result;
import com.student.scm.service.IScmPurchaseDetailService;
import com.student.scm.vo.ScmPurchaseDetailVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/purchase_detail")
public class ScmPurchaseDetailController {

    private final IScmPurchaseDetailService purchaseDetailService;

    public ScmPurchaseDetailController(IScmPurchaseDetailService purchaseDetailService) {
        this.purchaseDetailService = purchaseDetailService;
    }

    @GetMapping("/list/{orderId}")
    public Result<List<ScmPurchaseDetailVO>> listByOrderId(@PathVariable Long orderId) {
        return Result.success(purchaseDetailService.listByOrderId(orderId));
    }
}
