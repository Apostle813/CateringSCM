package com.student.scm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.student.scm.context.BaseContext;
import com.student.scm.dto.PurchaseOrderPageQueryDTO;
import com.student.scm.dto.PurchaseOrderRejectDTO;
import com.student.scm.dto.PurchaseOrderSubmitDTO;
import com.student.scm.entity.ScmInventory;
import com.student.scm.entity.ScmPurchaseDetail;
import com.student.scm.entity.ScmPurchaseOrder;
import com.student.scm.entity.ScmStockLog;
import com.student.scm.mapper.ScmInventoryMapper;
import com.student.scm.mapper.ScmPurchaseDetailMapper;
import com.student.scm.mapper.ScmPurchaseOrderMapper;
import com.student.scm.mapper.ScmStockLogMapper;
import com.student.scm.service.IScmInventoryService;
import com.student.scm.service.IScmOperationLogService;
import com.student.scm.service.IScmPurchaseDetailService;
import com.student.scm.service.IScmPurchaseOrderService;
import com.student.scm.service.IScmStockLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class ScmPurchaseOrderServiceImpl extends ServiceImpl<ScmPurchaseOrderMapper, ScmPurchaseOrder> implements IScmPurchaseOrderService {
    private IScmPurchaseDetailService purchaseDetailService;
    private IScmInventoryService inventoryService;
    private IScmStockLogService stockLogService;
    private IScmOperationLogService operationLogService;
    private ScmInventoryMapper inventoryMapper;
    private ScmPurchaseDetailMapper detailMapper;
    private ScmStockLogMapper stockLogMapper;

    public ScmPurchaseOrderServiceImpl(IScmPurchaseDetailService purchaseDetailService, IScmInventoryService inventoryService, IScmStockLogService stockLogService, IScmOperationLogService operationLogService, ScmInventoryMapper inventoryMapper, ScmPurchaseDetailMapper detailMapper, ScmStockLogMapper stockLogMapper) {
        this.purchaseDetailService = purchaseDetailService;
        this.inventoryService = inventoryService;
        this.stockLogService = stockLogService;
        this.operationLogService = operationLogService;
        this.inventoryMapper = inventoryMapper;
        this.detailMapper = detailMapper;
        this.stockLogMapper = stockLogMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitOrder(PurchaseOrderSubmitDTO dto) {
        Long currentUserId = BaseContext.getCurrentId();
        if (dto.getDetails() == null || dto.getDetails().isEmpty()) {
            throw new RuntimeException("采购明细不能为空");
        }
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (PurchaseOrderSubmitDTO.PurchaseDetailDTO detailDto : dto.getDetails()) {
            BigDecimal planQty = new BigDecimal(detailDto.getPlanQty());
            BigDecimal lineAmount = detailDto.getPrice().multiply(planQty);
            totalAmount = totalAmount.add(lineAmount);
        }


        ScmPurchaseOrder order = new ScmPurchaseOrder();

        String orderNo = "P" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + (new Random().nextInt(900) + 100);
        order.setOrderNo(orderNo);
        order.setSupplierId(dto.getSupplierId());
        order.setWarehouseId(dto.getWarehouseId());
        order.setTotalAmount(totalAmount);
        order.setStatus(0);
        order.setRemark(dto.getRemark());
        order.setCreateUser(currentUserId);

        this.save(order);

        List<ScmPurchaseDetail> detailList = new ArrayList<>();
        for (PurchaseOrderSubmitDTO.PurchaseDetailDTO detailDto : dto.getDetails()) {
            ScmPurchaseDetail detail = new ScmPurchaseDetail();

            detail.setOrderId(order.getId());
            detail.setMaterialId(detailDto.getMaterialId());
            detail.setPlanQty(detailDto.getPlanQty());
            detail.setPrice(detailDto.getPrice());

            detailList.add(detail);
        }

        purchaseDetailService.saveBatch(detailList);

        // 操作日志
        operationLogService.saveLog("PURCHASE_SUBMIT", "发起采购申请 单号:" + orderNo, "purchase_order", order.getId());
    }

    @Override
    public Page<ScmPurchaseOrder> queryPageByCondition(PurchaseOrderPageQueryDTO queryDTO) {
        Page<ScmPurchaseOrder> pageInfo = new Page<>(queryDTO.getPage(), queryDTO.getPageSize());

        LambdaQueryWrapper<ScmPurchaseOrder> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.like(StringUtils.hasText(queryDTO.getOrderNo()), ScmPurchaseOrder::getOrderNo, queryDTO.getOrderNo());
        queryWrapper.eq(queryDTO.getStatus() != null, ScmPurchaseOrder::getStatus, queryDTO.getStatus());
        queryWrapper.eq(queryDTO.getSupplierId() != null, ScmPurchaseOrder::getSupplierId, queryDTO.getSupplierId());
        queryWrapper.eq(queryDTO.getWarehouseId() != null, ScmPurchaseOrder::getWarehouseId, queryDTO.getWarehouseId());
        if (StringUtils.hasText(queryDTO.getStartDate())) {
            queryWrapper.ge(ScmPurchaseOrder::getCreateTime, queryDTO.getStartDate() + " 00:00:00");
        }
        if (StringUtils.hasText(queryDTO.getEndDate())) {
            queryWrapper.le(ScmPurchaseOrder::getCreateTime, queryDTO.getEndDate() + " 23:59:59");
        }
        queryWrapper.orderByDesc(ScmPurchaseOrder::getCreateTime);

        this.page(pageInfo, queryWrapper);

        return pageInfo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditPass(Long orderId) {
        ScmPurchaseOrder order = this.getById(orderId);
        if (order == null) {
            throw new RuntimeException("采购单不存在！");
        }
        if (order.getStatus() != 0) {
            throw new RuntimeException("该订单不是待审核状态，无法进行审核操作！");
        }
        order.setStatus(1);
        order.setAuditTime(LocalDateTime.now());
        Long currentUserId = BaseContext.getCurrentId();
        order.setAuditUser(currentUserId);
        this.updateById(order);

        operationLogService.saveLog("PURCHASE_AUDIT_PASS", "审核通过采购单 单号:" + order.getOrderNo(), "purchase_order", orderId);
    }

    @Override
    public BigDecimal getMonthPurchaseAmount() {
        return this.baseMapper.sumMonthPurchaseAmount();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectOrder(PurchaseOrderRejectDTO dto) {
        ScmPurchaseOrder order = this.getById(dto.getId());
        if (order != null && order.getStatus() == 0) {
            order.setStatus(9);
            order.setRemark(order.getRemark() + " [驳回原因: " + dto.getRejectReason() + "]");
            this.updateById(order);
        } else {
            throw new RuntimeException("单据状态异常，无法驳回");
        }

        operationLogService.saveLog("PURCHASE_REJECT", "驳回采购单 单号:" + order.getOrderNo() + " 原因:" + dto.getRejectReason(), "purchase_order", dto.getId());
    }

    @Override
    public void confirmPayment(Long orderId) {
        ScmPurchaseOrder order = this.getById(orderId);
        if (order != null && order.getPaymentStatus() == 0) {
            order.setPaymentStatus(1);
            this.updateById(order);
        }

        operationLogService.saveLog("PURCHASE_PAY", "财务打款 采购单号:" + order.getOrderNo(), "purchase_order", orderId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void executeInbound(Long orderId) {
        ScmPurchaseOrder order = this.getById(orderId);
        if (order == null || order.getStatus() == 2) {
            throw new RuntimeException("订单不存在或已入库");
        }

        List<ScmPurchaseDetail> details = detailMapper.selectList(
                new LambdaQueryWrapper<ScmPurchaseDetail>().eq(ScmPurchaseDetail::getOrderId, orderId)
        );

        Long currentUserId = BaseContext.getCurrentId() != null ? BaseContext.getCurrentId() : 1L;

        for (ScmPurchaseDetail detail : details) {
            Long warehouseId = order.getWarehouseId();
            Long materialId = detail.getMaterialId();
            Integer addQty = detail.getRealQty() > 0 ? detail.getRealQty() : detail.getPlanQty();

            ScmInventory inventory = inventoryMapper.selectOne(
                    new LambdaQueryWrapper<ScmInventory>()
                            .eq(ScmInventory::getWarehouseId, warehouseId)
                            .eq(ScmInventory::getMaterialId, materialId)
            );

            int beforeQty = 0;
            if (inventory == null) {
                inventory = new ScmInventory();
                inventory.setWarehouseId(warehouseId);
                inventory.setMaterialId(materialId);
                inventory.setQuantity(addQty);
                inventoryMapper.insert(inventory);
            } else {
                beforeQty = inventory.getQuantity();
                inventory.setQuantity(beforeQty + addQty);
                inventoryMapper.updateById(inventory);
            }

            ScmStockLog log = new ScmStockLog();
            log.setReferenceNo(order.getOrderNo());
            log.setType(1);
            log.setWarehouseId(warehouseId);
            log.setMaterialId(materialId);
            log.setChangeQty(addQty);
            log.setBeforeQty(beforeQty);
            log.setAfterQty(beforeQty + addQty);
            log.setOperatorId(currentUserId);
            stockLogMapper.insert(log);
        }

        order.setStatus(2);
        this.updateById(order);

        operationLogService.saveLog("PURCHASE_INBOUND", "采购入库 单号:" + order.getOrderNo(), "purchase_order", orderId);
    }

    @Override
    public Long countPending() {
        return this.baseMapper.countPending();
    }

    @Override
    public List<Map<String, Object>> getMonthlyPurchaseTrend() {
        return this.baseMapper.getMonthlyPurchaseTrend();
    }
}
