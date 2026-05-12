package com.student.scm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.student.scm.dto.InventoryAdjustDTO;
import com.student.scm.dto.InventoryOutboundDTO;
import com.student.scm.dto.InventoryPageQueryDTO;
import com.student.scm.entity.ScmInventory;
import com.student.scm.result.PageResult;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface IScmInventoryService extends IService<ScmInventory> {

    PageResult queryPageByCondition(InventoryPageQueryDTO queryDTO);

    void outbound(InventoryOutboundDTO dto);

    BigDecimal getTotalInventoryAsset();

    List<Map<String, Object>> getLowStockAlerts();

    void adjust(InventoryAdjustDTO dto);

    Integer getStockQty(Long warehouseId, Long materialId);

    List<Map<String, Object>> getWarehouseAssetDistribution();

    Integer getMonthInboundQty();

    Integer getMonthOutboundQty();
}
