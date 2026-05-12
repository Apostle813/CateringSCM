package com.student.scm.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.scm.dto.StorePageQueryDTO;
import com.student.scm.entity.ScmStore;
import com.student.scm.result.Result;
import com.student.scm.service.IScmOperationLogService;
import com.student.scm.service.IScmStoreService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/store")
public class ScmStoreController {

    private final IScmStoreService storeService;
    private final IScmOperationLogService operationLogService;

    public ScmStoreController(IScmStoreService storeService, IScmOperationLogService operationLogService) {
        this.storeService = storeService;
        this.operationLogService = operationLogService;
    }

    @GetMapping("/list")
    public Result<List<ScmStore>> listAll() {
        return Result.success(storeService.list(new LambdaQueryWrapper<ScmStore>().eq(ScmStore::getStatus, 1)));
    }

    @GetMapping("/page")
    public Result<Page<ScmStore>> page(StorePageQueryDTO queryDTO) {
        return Result.success(storeService.queryPageByCondition(queryDTO));
    }

    @PostMapping("/add")
    public Result<String> add(@RequestBody ScmStore store) {
        storeService.save(store);
        operationLogService.saveLog("STORE_ADD", "新增门店 " + store.getName(), "store", store.getId());
        return Result.success("添加成功");
    }

    @PutMapping("/update")
    public Result<String> update(@RequestBody ScmStore store) {
        storeService.updateById(store);
        operationLogService.saveLog("STORE_UPDATE", "修改门店 ID:" + store.getId(), "store", store.getId());
        return Result.success("修改成功");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        ScmStore store = new ScmStore();
        store.setId(id);
        store.setStatus(0);
        storeService.updateById(store);
        operationLogService.saveLog("STORE_DELETE", "停用门店 ID:" + id, "store", id);
        return Result.success("删除成功");
    }
}
