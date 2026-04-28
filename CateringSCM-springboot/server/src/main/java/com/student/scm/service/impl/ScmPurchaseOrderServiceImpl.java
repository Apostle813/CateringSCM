package com.student.scm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.student.scm.context.BaseContext;
import com.student.scm.dto.PurchaseOrderPageQueryDTO;
import com.student.scm.dto.PurchaseOrderRejectDTO;
import com.student.scm.dto.PurchaseOrderSubmitDTO;
import com.student.scm.dto.PurchaseQuickOrderDTO;
import com.student.scm.entity.ScmInventory;
import com.student.scm.entity.ScmPurchaseDetail;
import com.student.scm.entity.ScmPurchaseOrder;
import com.student.scm.entity.ScmStockLog;
import com.student.scm.mapper.ScmInventoryMapper;
import com.student.scm.mapper.ScmPurchaseDetailMapper;
import com.student.scm.mapper.ScmPurchaseOrderMapper;
import com.student.scm.mapper.ScmStockLogMapper;
import com.student.scm.service.IScmInventoryService;
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
import java.util.Random;

@Service
public class ScmPurchaseOrderServiceImpl extends ServiceImpl<ScmPurchaseOrderMapper, ScmPurchaseOrder> implements IScmPurchaseOrderService {
    private IScmPurchaseDetailService purchaseDetailService;
    private IScmInventoryService inventoryService;
    private IScmStockLogService stockLogService;
    private ScmInventoryMapper inventoryMapper;
    private ScmPurchaseDetailMapper detailMapper;
    private ScmStockLogMapper stockLogMapper;

    public ScmPurchaseOrderServiceImpl(IScmPurchaseDetailService purchaseDetailService, IScmInventoryService inventoryService, IScmStockLogService stockLogService, ScmInventoryMapper inventoryMapper, ScmPurchaseDetailMapper detailMapper, ScmStockLogMapper stockLogMapper) {
        this.purchaseDetailService = purchaseDetailService;
        this.inventoryService = inventoryService;
        this.stockLogService = stockLogService;
        this.inventoryMapper = inventoryMapper;
        this.detailMapper = detailMapper;
        this.stockLogMapper = stockLogMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 由于其同时处理多个数据库故要开启数据库事务管理。
    public void submitOrder(PurchaseOrderSubmitDTO dto) {
        Long currentUserId = BaseContext.getCurrentId();
        // 1. 校验前端传来的数据不能为空
        if (dto.getDetails() == null || dto.getDetails().isEmpty()) {
            throw new RuntimeException("采购明细不能为空");
        }

        // 2. 遍历计算订单的总金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (PurchaseOrderSubmitDTO.PurchaseDetailDTO detailDto : dto.getDetails()) {
            // 小计金额 = 单价 * 数量
            BigDecimal planQty = new BigDecimal(detailDto.getPlanQty());
            BigDecimal lineAmount = detailDto.getPrice().multiply(planQty);
            totalAmount = totalAmount.add(lineAmount);
        }

        // 3. 构建采购单主表信息并保存
        ScmPurchaseOrder order = new ScmPurchaseOrder();
        // 自动生成单号 (P + 年月日时分秒 + 3位随机数)，例如 P202603101736123
        String orderNo = "P" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + (new Random().nextInt(900) + 100);
        order.setOrderNo(orderNo);
        order.setSupplierId(dto.getSupplierId());
        order.setWarehouseId(dto.getWarehouseId());
        order.setTotalAmount(totalAmount);
        order.setStatus(0); // 【关键】状态 0 代表“待审核”
        order.setRemark(dto.getRemark());
        order.setCreateUser(currentUserId);

        // 【注意】保存主表！保存成功后，MyBatis-Plus 会自动把生成的 ID 塞回到 order 对象里
        this.save(order);

        // 4. 构建采购单明细列表并批量保存
        List<ScmPurchaseDetail> detailList = new ArrayList<>();
        for (PurchaseOrderSubmitDTO.PurchaseDetailDTO detailDto : dto.getDetails()) {
            ScmPurchaseDetail detail = new ScmPurchaseDetail();
            // 绑定刚刚生成的主表 ID
            detail.setOrderId(order.getId());
            detail.setMaterialId(detailDto.getMaterialId());
            detail.setPlanQty(detailDto.getPlanQty());
            detail.setPrice(detailDto.getPrice());

            detailList.add(detail);
        }

        // 批量保存明细到数据库
        purchaseDetailService.saveBatch(detailList);
    }

    @Override
    public Page<ScmPurchaseOrder> queryPageByCondition(PurchaseOrderPageQueryDTO queryDTO) {
        Page<ScmPurchaseOrder> pageInfo = new Page<>(queryDTO.getPage(), queryDTO.getPageSize());

        LambdaQueryWrapper<ScmPurchaseOrder> queryWrapper = new LambdaQueryWrapper<>();

        // 拼接单号模糊查询
        queryWrapper.like(StringUtils.hasText(queryDTO.getOrderNo()), ScmPurchaseOrder::getOrderNo, queryDTO.getOrderNo());
        // 拼接状态精准查询
        queryWrapper.eq(queryDTO.getStatus() != null, ScmPurchaseOrder::getStatus, queryDTO.getStatus());
        // 拼接排序规则 (按创建时间倒序)
        queryWrapper.orderByDesc(ScmPurchaseOrder::getCreateTime);

        // 3. 执行 MyBatis-Plus 的底部分页查询
        this.page(pageInfo, queryWrapper);

        // 4. 返回查好的结果
        return pageInfo;    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditPass(Long orderId) {
        // 1. 查询出这笔主订单
        ScmPurchaseOrder order = this.getById(orderId);
        if (order == null) {
            throw new RuntimeException("采购单不存在！");
        }
        if (order.getStatus() != 0) { // 0: 待审核
            throw new RuntimeException("该订单不是待审核状态，无法进行审核操作！");
        }
        // 2. 修改主表状态：0 -> 1(待入库)
        order.setStatus(1);
        order.setAuditTime(LocalDateTime.now());
        Long currentUserId = BaseContext.getCurrentId();
        order.setAuditUser(currentUserId);
        this.updateById(order);
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
            order.setStatus(9); // 9: 已驳回
            // 将驳回原因追加到备注里，方便前端展示
            order.setRemark(order.getRemark() + " [驳回原因: " + dto.getRejectReason() + "]");
            this.updateById(order);
        } else {
            throw new RuntimeException("单据状态异常，无法驳回");
        }
    }

    @Override
    public void confirmPayment(Long orderId) {
        ScmPurchaseOrder order = this.getById(orderId);
        if (order != null && order.getPaymentStatus() == 0) {
            order.setPaymentStatus(1); // 修改为已结算
            this.updateById(order);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void executeInbound(Long orderId) {
        // 1. 获取订单，检查状态
        ScmPurchaseOrder order = this.getById(orderId);
        if (order == null || order.getStatus() == 2) {
            throw new RuntimeException("订单不存在或已入库");
        }

        // 2. 获取该订单的所有采购明细
        List<ScmPurchaseDetail> details = detailMapper.selectList(
                new LambdaQueryWrapper<ScmPurchaseDetail>().eq(ScmPurchaseDetail::getOrderId, orderId)
        );

        Long currentUserId = BaseContext.getCurrentId() != null ? BaseContext.getCurrentId() : 1L;

        // 3. 遍历明细，增加库存并记录流水
        for (ScmPurchaseDetail detail : details) {
            Long warehouseId = order.getWarehouseId();
            Long materialId = detail.getMaterialId();
            Integer addQty = detail.getRealQty() > 0 ? detail.getRealQty() : detail.getPlanQty(); // 优先用实收数量

            // 查询是否已有该库存
            ScmInventory inventory = inventoryMapper.selectOne(
                    new LambdaQueryWrapper<ScmInventory>()
                            .eq(ScmInventory::getWarehouseId, warehouseId)
                            .eq(ScmInventory::getMaterialId, materialId)
            );

            int beforeQty = 0;
            if (inventory == null) {
                // 如果是新物料第一次入库，新增库存记录
                inventory = new ScmInventory();
                inventory.setWarehouseId(warehouseId);
                inventory.setMaterialId(materialId);
                inventory.setQuantity(addQty);
                inventoryMapper.insert(inventory);
            } else {
                // 如果已有库存，直接累加
                beforeQty = inventory.getQuantity();
                inventory.setQuantity(beforeQty + addQty);
                inventoryMapper.updateById(inventory);
            }

            // 记录入库流水 (Type = 1: 采购入库)
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

        // 4. 将订单状态改为 2 (已入库)
        order.setStatus(2);
        this.updateById(order);
    }
    @Transactional(rollbackFor = Exception.class)
    public void quickPurchase(PurchaseQuickOrderDTO dto) {
        ScmPurchaseOrder order = new ScmPurchaseOrder();
        order.setOrderNo("P" + System.currentTimeMillis());
        order.setSupplierId(dto.getSupplierId());
        order.setWarehouseId(dto.getWarehouseId());
        order.setTotalAmount(dto.getPrice().multiply(new BigDecimal(dto.getPlanQty())));
        order.setStatus(0);
        order.setPaymentStatus(0);
        order.setRemark("门店极速直采补货");
        this.save(order);

        ScmPurchaseDetail detail = new ScmPurchaseDetail();
        detail.setOrderId(order.getId());
        detail.setMaterialId(dto.getMaterialId());
        detail.setPlanQty(dto.getPlanQty());
        detail.setRealQty(dto.getPlanQty());
        detail.setPrice(dto.getPrice());
        detailMapper.insert(detail);
    }
}
