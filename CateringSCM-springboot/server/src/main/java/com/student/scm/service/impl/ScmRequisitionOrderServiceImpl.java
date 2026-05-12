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
import com.student.scm.service.IScmOperationLogService;
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
    private final IScmOperationLogService operationLogService;

    public ScmRequisitionOrderServiceImpl(IScmRequisitionDetailService detailService, IScmInventoryService inventoryService, IScmStockLogService stockLogService, IScmOperationLogService operationLogService) {
        this.detailService = detailService;
        this.inventoryService = inventoryService;
        this.stockLogService = stockLogService;
        this.operationLogService = operationLogService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitOrder(RequisitionOrderSubmitDTO dto) {
        if (dto.getDetails() == null || dto.getDetails().isEmpty()) {
            throw new RuntimeException("请购明细不能为空！");
        }
        Long currentUserId = BaseContext.getCurrentId();

        ScmRequisitionOrder order = new ScmRequisitionOrder();
        String orderNo = "OUT" + System.currentTimeMillis();
        order.setOrderNo(orderNo);
        order.setStoreId(dto.getStoreId());
        order.setWarehouseId(dto.getWarehouseId());
        order.setStatus(0);
        order.setPaymentStatus(0);
        order.setCreateTime(LocalDateTime.now());
        order.setCreateBy(currentUserId);
        this.save(order);

        for (RequisitionOrderSubmitDTO.RequisitionDetailDTO d : dto.getDetails()) {
            ScmRequisitionDetail detail = new ScmRequisitionDetail();
            detail.setOrderId(order.getId());
            detail.setMaterialId(d.getMaterialId());
            detail.setPlanQty(d.getPlanQty());
            detail.setRealQty(0);
            detailService.save(detail);
        }

        operationLogService.saveLog("REQUISITION_SUBMIT", "门店发起请购 单号:" + orderNo, "requisition_order", order.getId());
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
        if (queryDTO.getWarehouseId() != null) {
            wrapper.eq(ScmRequisitionOrder::getWarehouseId, queryDTO.getWarehouseId());
        }
        if (queryDTO.getStartDate() != null && !queryDTO.getStartDate().isEmpty()) {
            wrapper.ge(ScmRequisitionOrder::getCreateTime, queryDTO.getStartDate() + " 00:00:00");
        }
        if (queryDTO.getEndDate() != null && !queryDTO.getEndDate().isEmpty()) {
            wrapper.le(ScmRequisitionOrder::getCreateTime, queryDTO.getEndDate() + " 23:59:59");
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

        LambdaQueryWrapper<ScmRequisitionDetail> detailWrapper = new LambdaQueryWrapper<>();
        detailWrapper.eq(ScmRequisitionDetail::getOrderId, orderId);
        List<ScmRequisitionDetail> detailList = detailService.list(detailWrapper);

        Long warehouseId = order.getWarehouseId();
        Long currentUserId = BaseContext.getCurrentId();

        for (ScmRequisitionDetail detail : detailList) {
            Long materialId = detail.getMaterialId();
            Integer outQty = detail.getPlanQty();

            detail.setRealQty(outQty);
            detailService.updateById(detail);

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

        order.setStatus(1);
        this.updateById(order);

        operationLogService.saveLog("REQUISITION_AUDIT", "审核发货 出库单号:" + order.getOrderNo(), "requisition_order", orderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectOutbound(Long orderId) {
        ScmRequisitionOrder order = this.getById(orderId);
        if (order == null) throw new RuntimeException("单据不存在");
        if (order.getStatus() != 0) throw new RuntimeException("单据状态不是待审核，无法驳回");
        order.setStatus(9);
        this.updateById(order);

        operationLogService.saveLog("REQUISITION_REJECT", "驳回出库单 单号:" + order.getOrderNo(), "requisition_order", orderId);
    }

    @Override
    public void confirmPayment(Long orderId) {
        ScmRequisitionOrder order = this.getById(orderId);
        if (order == null) throw new RuntimeException("单据不存在");
        if (order.getPaymentStatus() == 1) throw new RuntimeException("已经结算过，无需重复结算");
        order.setPaymentStatus(1);
        this.updateById(order);

        operationLogService.saveLog("REQUISITION_PAY", "内部结算 出库单号:" + order.getOrderNo(), "requisition_order", orderId);
    }

    @Override
    public Long countPending() {
        return this.baseMapper.countPending();
    }
}
