package com.student.scm.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.scm.dto.MaterialPageQueryDTO;
import com.student.scm.entity.ScmMaterial;
import com.student.scm.result.Result;
import com.student.scm.service.IScmMaterialService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/material")
public class ScmMaterialController {

    private IScmMaterialService materialService;

    public ScmMaterialController(IScmMaterialService materialService) {
        this.materialService = materialService;
    }

    @GetMapping("/list")
    public Result<List<ScmMaterial>> listAll() {
        return Result.success(materialService.list());
    }

    /**
     * 分页模糊查询
     */
    @GetMapping("/page")
    public Result<Page<ScmMaterial>> page(MaterialPageQueryDTO queryDTO) {
        Page<ScmMaterial> pageInfo = materialService.queryPageByCondition(queryDTO);
        return Result.success(pageInfo);
    }

    /**
     * 新增
     */
    @PostMapping
    public Result<String> save(@RequestBody ScmMaterial material) {
        materialService.save(material);
        return Result.success("新增成功");
    }

    /**
     * 修改
     */
    @PutMapping
    public Result<String> update(@RequestBody ScmMaterial material) {
        materialService.updateById(material);
        return Result.success("修改成功");
    }

    /**
     * 逻辑删除
     */
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        materialService.removeById(id);
        return Result.success("删除成功");
    }
}
