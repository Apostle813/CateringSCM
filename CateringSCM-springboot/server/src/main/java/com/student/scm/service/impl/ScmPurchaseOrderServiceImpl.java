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
import com.student.scm.mapper.ScmPurchaseOrderMapper;
import com.student.scm.service.IScmInventoryService;
import com.student.scm.service.IScmPurchaseDetailService;
import com.student.scm.service.IScmPurchaseOrderService;
import com.student.scm.service.IScmStockLogService;
import lombok.Data;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Data
@Service
public class ScmPurchaseOrderServiceImpl extends ServiceImpl<ScmPurchaseOrderMapper, ScmPurchaseOrder> implements IScmPurchaseOrderService {
    private IScmPurchaseDetailService purchaseDetailService;
    private IScmInventoryService inventoryService;
    private IScmStockLogService stockLogService;

    public ScmPurchaseOrderServiceImpl(IScmPurchaseDetailService purchaseDetailService, IScmInventoryService inventoryService, IScmStockLogService stockLogService) {
        this.purchaseDetailService = purchaseDetailService;
        this.inventoryService = inventoryService;
        this.stockLogService = stockLogService;
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
    public void auditInbound(Long orderId) {
        // 1. 查询出这笔主订单，并进行【防重复提交】校验
        ScmPurchaseOrder order = this.getById(orderId);
        if (order == null) {
            throw new RuntimeException("采购单不存在！");
        }
        if (order.getStatus() != 0) { // 0: 待审核
            throw new RuntimeException("该订单不是待审核状态，无法进行入库操作！");
        }
        // 2. 修改主表状态：0 -> 2(已入库)，并记录审核时间
        order.setStatus(2);
        order.setAuditTime(LocalDateTime.now());
        Long currentUserId = BaseContext.getCurrentId();
        order.setAuditUser(currentUserId);
        this.updateById(order);

        // 3. 查出这笔订单关联的所有“采购明细”（买了哪些菜，买了多少）
        LambdaQueryWrapper<ScmPurchaseDetail> detailWrapper = new LambdaQueryWrapper<>();
        detailWrapper.eq(ScmPurchaseDetail::getOrderId, orderId);
        List<ScmPurchaseDetail> detailList = purchaseDetailService.list(detailWrapper);

        Long warehouseId = order.getWarehouseId(); // 目标仓库ID

        // 4. 核心循环：遍历每一行明细，挨个入库
        for (ScmPurchaseDetail detail : detailList) {
            Long materialId = detail.getMaterialId();
            Integer inQty = detail.getPlanQty(); // 假设计划数量即为实际入库数量

            // 顺手把明细表里的“实际入库数”也更新一下 (模拟真实ERP操作)
            detail.setRealQty(inQty);
            purchaseDetailService.updateById(detail);

            // ================= 5. 库存台账逻辑 =================
            // 去库存表里查一查：这个仓库里，以前有没有这种食材？
            LambdaQueryWrapper<ScmInventory> invWrapper = new LambdaQueryWrapper<>();
            invWrapper.eq(ScmInventory::getWarehouseId, warehouseId)
                    .eq(ScmInventory::getMaterialId, materialId);
            ScmInventory inventory = inventoryService.getOne(invWrapper);

            int beforeQty = 0; // 变动前数量，默认0

            if (inventory == null) {
                // 如果是第一次采购这种食材：直接 insert 一条新记录
                inventory = new ScmInventory();
                inventory.setWarehouseId(warehouseId);
                inventory.setMaterialId(materialId);
                inventory.setQuantity(inQty);
                inventoryService.save(inventory);
            } else {
                // 如果以前买过这种食材：取出原来的数量，累加后再 update
                beforeQty = inventory.getQuantity();
                inventory.setQuantity(beforeQty + inQty);
                inventoryService.updateById(inventory);
            }

            // ================= 6. 记录库存流水 =================
            ScmStockLog stockLog = new ScmStockLog();
            stockLog.setReferenceNo(order.getOrderNo()); // 绑定关联单号
            stockLog.setType(1);                     // 1 代表采购入库
            stockLog.setWarehouseId(warehouseId);
            stockLog.setMaterialId(materialId);
            stockLog.setChangeQty(inQty);            // 增加了多少
            stockLog.setBeforeQty(beforeQty);        // 变动前有多少
            stockLog.setAfterQty(beforeQty + inQty); // 变动后有多少
            stockLog.setOperatorId(currentUserId);//操作者id

            stockLogService.save(stockLog);
        }
    }

    @Override
    public BigDecimal getMonthPurchaseAmount() {
        return this.baseMapper.sumMonthPurchaseAmount();
    }

    @Override
    public void rejectOrder(PurchaseOrderRejectDTO dto) {
        ScmPurchaseOrder order = this.getById(dto.getId());
        if (order == null) {
            throw new RuntimeException("采购单不存在！");
        }
        if (order.getStatus() != 0) { // 0: 待审核
            throw new RuntimeException("只有待审核状态的订单才能被驳回！");
        }

        // 2. 修改状态为 9 (已驳回)
        order.setStatus(9);

        // 3. 记录审核人和审核时间
        order.setAuditTime(LocalDateTime.now());
        order.setAuditUser(BaseContext.getCurrentId());

        // 4. 将驳回原因追加到备注字段中。
        String originalRemark = order.getRemark() == null ? "" : order.getRemark() + " | ";
        order.setRemark(originalRemark + "【驳回原因】: " + dto.getRejectReason());

        this.updateById(order);
    }
}
