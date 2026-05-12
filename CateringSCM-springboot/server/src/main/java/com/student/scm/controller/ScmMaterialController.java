package com.student.scm.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.scm.dto.MaterialPageQueryDTO;
import com.student.scm.entity.ScmMaterial;
import com.student.scm.result.Result;
import com.student.scm.service.IScmMaterialService;
import com.student.scm.service.IScmOperationLogService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/material")
public class ScmMaterialController {

    private IScmMaterialService materialService;
    private IScmOperationLogService operationLogService;

    public ScmMaterialController(IScmMaterialService materialService, IScmOperationLogService operationLogService) {
        this.materialService = materialService;
        this.operationLogService = operationLogService;
    }

    @GetMapping("/list")
    public Result<List<ScmMaterial>> listAll() {
        return Result.success(materialService.list());
    }

    @GetMapping("/page")
    public Result<Page<ScmMaterial>> page(MaterialPageQueryDTO queryDTO) {
        Page<ScmMaterial> pageInfo = materialService.queryPageByCondition(queryDTO);
        return Result.success(pageInfo);
    }

    @PostMapping
    public Result<String> save(@RequestBody ScmMaterial material) {
        materialService.save(material);
        operationLogService.saveLog("MATERIAL_ADD", "新增食材 " + material.getName(), "material", material.getId());
        return Result.success("新增成功");
    }

    @PutMapping
    public Result<String> update(@RequestBody ScmMaterial material) {
        materialService.updateById(material);
        operationLogService.saveLog("MATERIAL_UPDATE", "修改食材 ID:" + material.getId(), "material", material.getId());
        return Result.success("修改成功");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        materialService.removeById(id);
        operationLogService.saveLog("MATERIAL_DELETE", "删除食材 ID:" + id, "material", id);
        return Result.success("删除成功");
    }
}
