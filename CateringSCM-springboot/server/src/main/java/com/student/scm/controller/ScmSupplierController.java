package com.student.scm.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.student.scm.entity.ScmSupplier;
import com.student.scm.result.Result;
import com.student.scm.service.IScmSupplierService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

@RestController
@RequestMapping("/supplier")
public class ScmSupplierController {
    private IScmSupplierService supplierService;

    public ScmSupplierController(IScmSupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @GetMapping("/list")
    public Result<List<ScmSupplier>> listAll() {
        return Result.success(supplierService.list(new LambdaQueryWrapper<ScmSupplier>().eq(ScmSupplier::getStatus, 1)));
    }

    @GetMapping("/page")
    public Result<Page<ScmSupplier>> page(@RequestParam(defaultValue = "1") Integer page,
                                          @RequestParam(defaultValue = "10") Integer pageSize,
                                          String name) {
        Page<ScmSupplier> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<ScmSupplier> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(name != null && !name.isEmpty(), ScmSupplier::getName, name);
        queryWrapper.orderByDesc(ScmSupplier::getCreateTime);
        return Result.success(supplierService.page(pageInfo, queryWrapper));
    }

    @PostMapping("/add")
    public Result<String> add(@RequestBody ScmSupplier supplier) {
        supplierService.save(supplier);
        return Result.success("添加成功");
    }

    @PutMapping("/update")
    public Result<String> update(@RequestBody ScmSupplier supplier) {
        supplierService.updateById(supplier);
        return Result.success("修改成功");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        ScmSupplier supplier = new ScmSupplier();
        supplier.setId(id);
        supplier.setStatus(0); // 逻辑删除或标记为已终止
        supplierService.updateById(supplier);
        return Result.success("删除成功");
    }
}
