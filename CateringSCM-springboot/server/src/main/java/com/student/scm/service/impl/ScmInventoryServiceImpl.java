package com.student.scm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import com.student.scm.service.IScmStockLogService;
import com.student.scm.vo.ScmInventoryVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ScmInventoryServiceImpl extends ServiceImpl<ScmInventoryMapper, ScmInventory> implements IScmInventoryService {
    private final IScmStockLogService stockLogService;
    private final ScmWarehouseMapper  warehouseMapper;
    private final ScmMaterialMapper materialMapper;

    public ScmInventoryServiceImpl(IScmStockLogService stockLogService, ScmWarehouseMapper warehouseMapper, ScmMaterialMapper materialMapper) {
        this.stockLogService = stockLogService;
        this.warehouseMapper = warehouseMapper;
        this.materialMapper = materialMapper;
    }



    @Override
    public PageResult queryPageByCondition(InventoryPageQueryDTO queryDTO) {
        Page<ScmInventory> pageInfo = new Page<>(queryDTO.getPage(), queryDTO.getPageSize());
        LambdaQueryWrapper<ScmInventory> queryWrapper = new LambdaQueryWrapper<>();

        // 如果前端传了仓库ID或物资名称，在这里加条件
        if (queryDTO.getWarehouseId() != null) {
            queryWrapper.eq(ScmInventory::getWarehouseId, queryDTO.getWarehouseId());
        }
        queryWrapper.orderByAsc(ScmInventory::getId);
        queryWrapper.eq(queryDTO.getWarehouseId() != null, ScmInventory::getWarehouseId, queryDTO.getWarehouseId());
        queryWrapper.eq(queryDTO.getMaterialId() != null, ScmInventory::getMaterialId, queryDTO.getMaterialId());
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
        // 1. 查询当前库存
        LambdaQueryWrapper<ScmInventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ScmInventory::getWarehouseId, warehouseId)
                .eq(ScmInventory::getMaterialId, materialId);
        ScmInventory inventory = this.getOne(wrapper);
        // 2. 核心校验：防超卖 (不能扣成负数)
        if (inventory == null || inventory.getQuantity() < outQty) {
            throw new RuntimeException("操作失败：该仓库的此食材库存不足！当前余量：" +
                    (inventory == null ? 0 : inventory.getQuantity()));
        }
        int beforeQty = inventory.getQuantity();

        // 3. 扣减台账库存
        inventory.setQuantity(beforeQty - outQty);
        this.updateById(inventory);
        Long currentUserId = BaseContext.getCurrentId();
        // 4. 记录出库流水 (type = 2)
        ScmStockLog stockLog = new ScmStockLog();
        stockLog.setReferenceNo(dto.getReferenceNo()); // 比如填写："后厨张大厨领用"
        stockLog.setType(2);                           // 2: 领料出库
        stockLog.setWarehouseId(warehouseId);
        stockLog.setMaterialId(materialId);
        stockLog.setChangeQty(-outQty);                // 注意：出库流水记录为负数！
        stockLog.setBeforeQty(beforeQty);
        stockLog.setAfterQty(beforeQty - outQty);
        stockLog.setOperatorId(currentUserId);

        stockLogService.save(stockLog);
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
    }
}
