package com.student.scm.controller;


import com.student.scm.entity.ScmWarehouse;
import com.student.scm.result.Result;
import com.student.scm.service.IScmWarehouseService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/warehouse")
public class ScmWarehouseController {
    private IScmWarehouseService warehouseService;

    public ScmWarehouseController(IScmWarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    @GetMapping("/list")
    public Result<List<ScmWarehouse>> listAll() {
        return Result.success(warehouseService.list());
    }

    @org.springframework.web.bind.annotation.PostMapping
    public Result<?> addWarehouse(@org.springframework.web.bind.annotation.RequestBody ScmWarehouse warehouse) {
        warehouseService.save(warehouse);
        return Result.success();
    }

    @org.springframework.web.bind.annotation.PutMapping("/update")
    public Result<?> updateWarehouse(@org.springframework.web.bind.annotation.RequestBody ScmWarehouse warehouse) {
        warehouseService.updateById(warehouse);
        return Result.success();
    }
}
