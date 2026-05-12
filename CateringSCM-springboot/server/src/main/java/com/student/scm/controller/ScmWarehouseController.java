package com.student.scm.controller;


import com.student.scm.entity.ScmWarehouse;
import com.student.scm.result.Result;
import com.student.scm.service.IScmOperationLogService;
import com.student.scm.service.IScmWarehouseService;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/warehouse")
public class ScmWarehouseController {
    private IScmWarehouseService warehouseService;
    private IScmOperationLogService operationLogService;

    public ScmWarehouseController(IScmWarehouseService warehouseService, IScmOperationLogService operationLogService) {
        this.warehouseService = warehouseService;
        this.operationLogService = operationLogService;
    }

    @GetMapping("/list")
    public Result<List<ScmWarehouse>> listAll() {
        return Result.success(warehouseService.list());
    }

    @PostMapping
    public Result<?> addWarehouse(@RequestBody ScmWarehouse warehouse) {
        warehouseService.save(warehouse);
        operationLogService.saveLog("WAREHOUSE_ADD", "新增仓库 " + warehouse.getName(), "warehouse", warehouse.getId());
        return Result.success();
    }

    @PutMapping("/update")
    public Result<?> updateWarehouse(@RequestBody ScmWarehouse warehouse) {
        warehouseService.updateById(warehouse);
        operationLogService.saveLog("WAREHOUSE_UPDATE", "修改仓库 ID:" + warehouse.getId(), "warehouse", warehouse.getId());
        return Result.success();
    }
}
