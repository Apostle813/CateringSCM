package com.student.scm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.student.scm.context.BaseContext;
import com.student.scm.dto.InventoryAdjustDTO;
import com.student.scm.dto.InventoryOutboundDTO;
import com.student.scm.dto.InventoryPageQueryDTO;
import com.student.scm.entity.ScmInventory;
import com.student.scm.entity.ScmMaterial;
import com.student.scm.entity.ScmStockLog;
import com.student.scm.entity.ScmWarehouse;
import com.student.scm.mapper.ScmInventoryMapper;
import com.student.scm.mapper.ScmMaterialMapper;
import com.student.scm.mapper.ScmWarehouseMapper;
import com.student.scm.result.PageResult;
import com.student.scm.service.IScmInventoryService;
import com.student.scm.service.IScmOperationLogService;
import com.student.scm.service.IScmStockLogService;
import com.student.scm.vo.ScmInventoryVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ScmInventoryServiceImpl extends ServiceImpl<ScmInventoryMapper, ScmInventory> implements IScmInventoryService {
    private final IScmStockLogService stockLogService;
    private final IScmOperationLogService operationLogService;
    private final ScmWarehouseMapper warehouseMapper;
    private final ScmMaterialMapper materialMapper;

    public ScmInventoryServiceImpl(IScmStockLogService stockLogService, IScmOperationLogService operationLogService, ScmWarehouseMapper warehouseMapper, ScmMaterialMapper materialMapper) {
        this.stockLogService = stockLogService;
        this.operationLogService = operationLogService;
        this.warehouseMapper = warehouseMapper;
        this.materialMapper = materialMapper;
    }


    @Override
    public PageResult queryPageByCondition(InventoryPageQueryDTO queryDTO) {
        Page<ScmInventory> pageInfo = new Page<>(queryDTO.getPage(), queryDTO.getPageSize());
        LambdaQueryWrapper<ScmInventory> queryWrapper = new LambdaQueryWrapper<>();

        // 按仓库筛选
        queryWrapper.eq(queryDTO.getWarehouseId() != null, ScmInventory::getWarehouseId, queryDTO.getWarehouseId())
                .eq(queryDTO.getMaterialId() != null, ScmInventory::getMaterialId, queryDTO.getMaterialId());

        // 新增：按物料名称模糊查询（需要先查询符合条件的物料ID）
        if (StringUtils.hasText(queryDTO.getMaterialName())) {
            LambdaQueryWrapper<ScmMaterial> materialWrapper = new LambdaQueryWrapper<>();
            materialWrapper.like(ScmMaterial::getName, queryDTO.getMaterialName());
            List<ScmMaterial> materials = materialMapper.selectList(materialWrapper);
            if (!materials.isEmpty()) {
                List<Long> materialIds = materials.stream().map(ScmMaterial::getId).collect(Collectors.toList());
                queryWrapper.in(ScmInventory::getMaterialId, materialIds);
            } else {
                // 如果没有符合条件的物料，直接返回空结果
                return new PageResult(0L, Collections.emptyList());
            }
        }

        // 新增：按分类筛选（需要先查询符合条件的物料ID）
        if (StringUtils.hasText(queryDTO.getCategory())) {
            LambdaQueryWrapper<ScmMaterial> materialWrapper = new LambdaQueryWrapper<>();
            materialWrapper.eq(ScmMaterial::getCategory, queryDTO.getCategory());
            List<ScmMaterial> materials = materialMapper.selectList(materialWrapper);
            if (!materials.isEmpty()) {
                List<Long> materialIds = materials.stream().map(ScmMaterial::getId).collect(Collectors.toList());
                queryWrapper.in(ScmInventory::getMaterialId, materialIds);
            } else {
                // 如果没有符合条件的物料，直接返回空结果
                return new PageResult(0L, Collections.emptyList());
            }
        }

        queryWrapper.orderByAsc(ScmInventory::getId);

        this.page(pageInfo, queryWrapper);

        List<ScmInventoryVO> voList = pageInfo.getRecords().stream().map(entity -> {
            ScmInventoryVO vo = new ScmInventoryVO();
            BeanUtils.copyProperties(entity, vo);

            ScmWarehouse w = warehouseMapper.selectById(entity.getWarehouseId());
            if (w != null) vo.setWarehouseName(w.getName());

            ScmMaterial m = materialMapper.selectById(entity.getMaterialId());
            if (m != null) {
                vo.setMaterialName(m.getName());
                vo.setCategory(m.getCategory());
                vo.setUnit(m.getUnit());
                vo.setSpec(m.getSpec());
                vo.setPrice(m.getPrice());
            }

            return vo;
        }).collect(Collectors.toList());

        return new PageResult(pageInfo.getTotal(), voList);
    }

    @Override
    @Transactional(rollbackFor=Exception.class)
    public void outbound(InventoryOutboundDTO dto) {
        Long warehouseId = dto.getWarehouseId();
        Long materialId = dto.getMaterialId();
        Integer outQty = dto.getOutQty();
        LambdaQueryWrapper<ScmInventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ScmInventory::getWarehouseId, warehouseId)
                .eq(ScmInventory::getMaterialId, materialId);
        ScmInventory inventory = this.getOne(wrapper);

        if (inventory == null || inventory.getQuantity() < outQty) {
            throw new RuntimeException("操作失败：该仓库的此食材库存不足！");
        }
        int beforeQty = inventory.getQuantity();

        boolean updateResult = this.update(new LambdaUpdateWrapper<ScmInventory>()
                .eq(ScmInventory::getId, inventory.getId())
                .ge(ScmInventory::getQuantity, outQty)
                .setSql("quantity = quantity - " + outQty));

        if (!updateResult) {
            throw new RuntimeException("系统繁忙或库存已被其他操作员扣减，请重试！");
        }
        Long currentUserId = BaseContext.getCurrentId();
        ScmStockLog stockLog = new ScmStockLog();
        stockLog.setReferenceNo(dto.getReferenceNo());
        stockLog.setType(2);
        stockLog.setWarehouseId(warehouseId);
        stockLog.setMaterialId(materialId);
        stockLog.setChangeQty(-outQty);
        stockLog.setBeforeQty(beforeQty);
        stockLog.setAfterQty(beforeQty - outQty);
        stockLog.setOperatorId(currentUserId);

        stockLogService.save(stockLog);

        operationLogService.saveLog("INVENTORY_OUTBOUND", "手动出库 物料ID:" + materialId + " 数量:" + outQty + " 参考:" + dto.getReferenceNo(), "inventory", materialId);
    }

    @Override
    public BigDecimal getTotalInventoryAsset() {
        return this.baseMapper.sumTotalInventoryAsset();
    }

    @Override
    public List<Map<String, Object>> getLowStockAlerts() {
        return this.baseMapper.getLowStockAlerts();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adjust(InventoryAdjustDTO dto) {
        Long warehouseId = dto.getWarehouseId();
        Long materialId = dto.getMaterialId();
        Integer realQty = dto.getRealQty();
        LambdaQueryWrapper<ScmInventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ScmInventory::getWarehouseId, warehouseId)
                .eq(ScmInventory::getMaterialId, materialId);
        ScmInventory inventory = this.getOne(wrapper);

        if (inventory == null) {
            throw new RuntimeException("食材不存在!");
        }
        int beforeQty = inventory.getQuantity();
        int changeQty = realQty - beforeQty;
        if (changeQty == 0) {
            return;
        }
        inventory.setQuantity(realQty);
        this.updateById(inventory);
        ScmStockLog stockLog = new ScmStockLog();
        stockLog.setReferenceNo("盘点调整：" + dto.getReason());
        stockLog.setType(3);
        stockLog.setWarehouseId(warehouseId);
        stockLog.setMaterialId(materialId);
        stockLog.setChangeQty(changeQty);
        stockLog.setBeforeQty(beforeQty);
        stockLog.setAfterQty(realQty);
        stockLog.setOperatorId(BaseContext.getCurrentId());

        stockLogService.save(stockLog);

        operationLogService.saveLog("INVENTORY_ADJUST", "盘点调整 物料ID:" + materialId + " 原:" + beforeQty + "->" + realQty + " 原因:" + dto.getReason(), "inventory", materialId);
    }

    @Override
    public Integer getStockQty(Long warehouseId, Long materialId) {
        LambdaQueryWrapper<ScmInventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ScmInventory::getWarehouseId, warehouseId)
                .eq(ScmInventory::getMaterialId, materialId);
        ScmInventory inventory = this.getOne(wrapper);
        return inventory != null ? inventory.getQuantity() : 0;
    }

    @Override
    public List<Map<String, Object>> getWarehouseAssetDistribution() {
        return this.baseMapper.getWarehouseAssetDistribution();
    }

    @Override
    public Integer getMonthInboundQty() {
        return this.baseMapper.getMonthInboundQty();
    }

    @Override
    public Integer getMonthOutboundQty() {
        return this.baseMapper.getMonthOutboundQty();
    }
}
