package com.student.scm.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.scm.dto.InventoryAdjustDTO;
import com.student.scm.dto.InventoryOutboundDTO;
import com.student.scm.dto.InventoryPageQueryDTO;
import com.student.scm.entity.ScmInventory;
import com.student.scm.result.Result;
import com.student.scm.service.IScmInventoryService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inventory")
public class ScmInventoryController {
    private IScmInventoryService inventoryService;

    public ScmInventoryController(IScmInventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }
    @GetMapping("/page")
    public Result<Page<ScmInventory>> page(InventoryPageQueryDTO queryDTO) {
        Page<ScmInventory> pageInfo= inventoryService.queryPageByCondition(queryDTO);
        return Result.success(pageInfo);
    }
    @PostMapping("/outbound")
    public Result<String> outbound(@RequestBody InventoryOutboundDTO dto) {
        inventoryService.outbound(dto);
        return Result.success("出库成功！");
    }

    @PostMapping("/adjust")
    public Result<String> adjust(@RequestBody InventoryAdjustDTO dto) {
        inventoryService.adjust(dto);
        return Result.success("盘点调整成功！");
    }
}
