package com.student.scm.controller;


import com.student.scm.dto.StockLogPageQueryDTO;
import com.student.scm.result.PageResult;
import com.student.scm.result.Result;
import com.student.scm.service.IScmStockLogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping("/stock_log")
public class ScmStockLogController {
    IScmStockLogService stockLogService;

    public ScmStockLogController(IScmStockLogService stockLogService) {
        this.stockLogService = stockLogService;
    }

    @GetMapping("/page")
    public Result<PageResult> page(StockLogPageQueryDTO queryDTO) {
        PageResult pageInfo = stockLogService.queryPageByCondition(queryDTO);
        return Result.success(pageInfo);
    }
}
