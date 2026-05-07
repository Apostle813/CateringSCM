package com.student.scm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.student.scm.context.BaseContext;
import com.student.scm.dto.RequisitionOrderPageQueryDTO;
import com.student.scm.dto.RequisitionOrderSubmitDTO;
import com.student.scm.entity.ScmInventory;
import com.student.scm.entity.ScmRequisitionDetail;
import com.student.scm.entity.ScmRequisitionOrder;
import com.student.scm.entity.ScmStockLog;
import com.student.scm.mapper.ScmRequisitionOrderMapper;
import com.student.scm.service.IScmInventoryService;
import com.student.scm.service.IScmRequisitionDetailService;
import com.student.scm.service.IScmRequisitionOrderService;
import com.student.scm.service.IScmStockLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ScmRequisitionOrderServiceImpl extends ServiceImpl<ScmRequisitionOrderMapper, ScmRequisitionOrder> implements IScmRequisitionOrderService {

    private final IScmRequisitionDetailService detailService;
    private final IScmInventoryService inventoryService;
    private final IScmStockLogService stockLogService;

    public ScmRequisitionOrderServiceImpl(IScmRequisitionDetailService detailService, IScmInventoryService inventoryService, IScmStockLogService stockLogService) {
        this.detailService = detailService;
        this.inventoryService = inventoryService;
        this.stockLogService = stockLogService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitOrder(RequisitionOrderSubmitDTO dto) {
        if (dto.getDetails() == null || dto.getDetails().isEmpty()) {
            throw new RuntimeException("请购明细不能为空！");
        }
        Long currentUserId = BaseContext.getCurrentId();
        
        // 1. 插入主表
        ScmRequisitionOrder order = new ScmRequisitionOrder();
        order.setOrderNo("OUT" + System.currentTimeMillis());
        order.setStoreId(dto.getStoreId());
        order.setWarehouseId(dto.getWarehouseId());
        order.setStatus(0); // 待审核
        order.setPaymentStatus(0); // 未结算
        order.setCreateTime(LocalDateTime.now());
        order.setCreateBy(currentUserId);
        this.save(order);

        // 2. 插入明细表
        for (RequisitionOrderSubmitDTO.RequisitionDetailDTO d : dto.getDetails()) {
            ScmRequisitionDetail detail = new ScmRequisitionDetail();
            detail.setOrderId(order.getId());
            detail.setMaterialId(d.getMaterialId());
            detail.setPlanQty(d.getPlanQty());
            detail.setRealQty(0);
            detailService.save(detail);
        }
    }

    @Override
    public Page<ScmRequisitionOrder> queryPageByCondition(RequisitionOrderPageQueryDTO queryDTO) {
        Page<ScmRequisitionOrder> page = new Page<>(queryDTO.getPage(), queryDTO.getPageSize());
        LambdaQueryWrapper<ScmRequisitionOrder> wrapper = new LambdaQueryWrapper<>();
        
        if (queryDTO.getOrderNo() != null && !queryDTO.getOrderNo().isEmpty()) {
            wrapper.like(ScmRequisitionOrder::getOrderNo, queryDTO.getOrderNo());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq(ScmRequisitionOrder::getStatus, queryDTO.getStatus());
        }
        wrapper.orderByDesc(ScmRequisitionOrder::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditOutbound(Long orderId) {
        ScmRequisitionOrder order = this.getById(orderId);
        if (order == null) throw new RuntimeException("单据不存在");
        if (order.getStatus() != 0) throw new RuntimeException("单据状态不是待审核，无法出库");

        // 查找明细
        LambdaQueryWrapper<ScmRequisitionDetail> detailWrapper = new LambdaQueryWrapper<>();
        detailWrapper.eq(ScmRequisitionDetail::getOrderId, orderId);
        List<ScmRequisitionDetail> detailList = detailService.list(detailWrapper);

        Long warehouseId = order.getWarehouseId();
        Long currentUserId = BaseContext.getCurrentId();

        for (ScmRequisitionDetail detail : detailList) {
            Long materialId = detail.getMaterialId();
            Integer outQty = detail.getPlanQty(); // 假设计划量为实际出库量

            detail.setRealQty(outQty);
            detailService.updateById(detail);

            // 扣减库存
            LambdaQueryWrapper<ScmInventory> invWrapper = new LambdaQueryWrapper<>();
            invWrapper.eq(ScmInventory::getWarehouseId, warehouseId)
                      .eq(ScmInventory::getMaterialId, materialId);
            ScmInventory inventory = inventoryService.getOne(invWrapper);

            if (inventory == null || inventory.getQuantity() < outQty) {
                throw new RuntimeException("仓库物资库存不足！材料ID: " + materialId);
            }

            int beforeQty = inventory.getQuantity();
            inventory.setQuantity(beforeQty - outQty);
            inventoryService.updateById(inventory);

            // 记录出库流水 (type=2 代表出库)
            ScmStockLog stockLog = new ScmStockLog();
            stockLog.setReferenceNo(order.getOrderNo());
            stockLog.setType(2); 
            stockLog.setWarehouseId(warehouseId);
            stockLog.setMaterialId(materialId);
            stockLog.setChangeQty(-outQty);
            stockLog.setBeforeQty(beforeQty);
            stockLog.setAfterQty(beforeQty - outQty);
            stockLog.setOperatorId(currentUserId);
            stockLogService.save(stockLog);
        }

        order.setStatus(1); // 已配送出库
        this.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectOutbound(Long orderId) {
        ScmRequisitionOrder order = this.getById(orderId);
        if (order == null) throw new RuntimeException("单据不存在");
        if (order.getStatus() != 0) throw new RuntimeException("单据状态不是待审核，无法驳回");
        order.setStatus(9); // 9表示已驳回
        this.updateById(order);
    }

    @Override
    public void confirmPayment(Long orderId) {
        ScmRequisitionOrder order = this.getById(orderId);
        if (order == null) throw new RuntimeException("单据不存在");
        if (order.getPaymentStatus() == 1) throw new RuntimeException("已经结算过，无需重复结算");
        order.setPaymentStatus(1);
        this.updateById(order);
    }
}
