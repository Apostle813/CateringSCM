package com.student.scm.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.student.scm.dto.SupplierPageQueryDTO;
import com.student.scm.entity.ScmSupplier;
import com.student.scm.result.Result;
import com.student.scm.service.IScmOperationLogService;
import com.student.scm.service.IScmSupplierService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/supplier")
public class ScmSupplierController {
    private IScmSupplierService supplierService;
    private IScmOperationLogService operationLogService;

    public ScmSupplierController(IScmSupplierService supplierService, IScmOperationLogService operationLogService) {
        this.supplierService = supplierService;
        this.operationLogService = operationLogService;
    }

    @GetMapping("/list")
    public Result<List<ScmSupplier>> listAll() {
        return Result.success(supplierService.list(new LambdaQueryWrapper<ScmSupplier>().eq(ScmSupplier::getStatus, 1)));
    }

    @GetMapping("/page")
    public Result<com.baomidou.mybatisplus.extension.plugins.pagination.Page<ScmSupplier>> page(SupplierPageQueryDTO queryDTO) {
        return Result.success(supplierService.queryPageByCondition(queryDTO));
    }

    @PostMapping("/add")
    public Result<String> add(@RequestBody ScmSupplier supplier) {
        supplierService.save(supplier);
        operationLogService.saveLog("SUPPLIER_ADD", "新增供应商 " + supplier.getName(), "supplier", supplier.getId());
        return Result.success("添加成功");
    }

    @PutMapping("/update")
    public Result<String> update(@RequestBody ScmSupplier supplier) {
        supplierService.updateById(supplier);
        operationLogService.saveLog("SUPPLIER_UPDATE", "修改供应商 ID:" + supplier.getId(), "supplier", supplier.getId());
        return Result.success("修改成功");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        ScmSupplier supplier = new ScmSupplier();
        supplier.setId(id);
        supplier.setStatus(0);
        supplierService.updateById(supplier);
        operationLogService.saveLog("SUPPLIER_DELETE", "停用供应商 ID:" + id, "supplier", id);
        return Result.success("删除成功");
    }
}
