package com.student.scm.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.scm.dto.OperationLogPageQueryDTO;
import com.student.scm.entity.ScmOperationLog;
import com.student.scm.result.Result;
import com.student.scm.service.IScmOperationLogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/operation_log")
public class ScmOperationLogController {

    private final IScmOperationLogService operationLogService;

    public ScmOperationLogController(IScmOperationLogService operationLogService) {
        this.operationLogService = operationLogService;
    }

    @GetMapping("/page")
    public Result<Page<ScmOperationLog>> page(OperationLogPageQueryDTO queryDTO) {
        return Result.success(operationLogService.queryPageByCondition(queryDTO));
    }
}
