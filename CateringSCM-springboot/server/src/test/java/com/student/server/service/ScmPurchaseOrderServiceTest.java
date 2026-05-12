package com.student.server.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.student.scm.context.BaseContext;
import com.student.scm.dto.PurchaseOrderRejectDTO;
import com.student.scm.dto.PurchaseOrderSubmitDTO;
import com.student.scm.entity.ScmPurchaseOrder;
import com.student.scm.mapper.ScmInventoryMapper;
import com.student.scm.mapper.ScmPurchaseDetailMapper;
import com.student.scm.mapper.ScmPurchaseOrderMapper;
import com.student.scm.mapper.ScmStockLogMapper;
import com.student.scm.service.*;
import com.student.scm.service.impl.ScmPurchaseOrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScmPurchaseOrderServiceTest {

    @Mock private IScmPurchaseDetailService purchaseDetailService;
    @Mock private IScmInventoryService inventoryService;
    @Mock private IScmStockLogService stockLogService;
    @Mock private IScmOperationLogService operationLogService;
    @Mock private ScmInventoryMapper inventoryMapper;
    @Mock private ScmPurchaseDetailMapper detailMapper;
    @Mock private ScmStockLogMapper stockLogMapper;
    @Mock private ScmPurchaseOrderMapper purchaseOrderMapper;

    private ScmPurchaseOrderServiceImpl purchaseOrderService;

    @BeforeEach
    void setUp() throws Exception {
        purchaseOrderService = new ScmPurchaseOrderServiceImpl(
                purchaseDetailService, inventoryService, stockLogService,
                operationLogService, inventoryMapper, detailMapper, stockLogMapper
        );
        // 向上搜索类层次，找到 baseMapper 字段并注入
        Class<?> clazz = purchaseOrderService.getClass();
        while (clazz != null) {
            try {
                java.lang.reflect.Field f = clazz.getDeclaredField("baseMapper");
                f.setAccessible(true);
                f.set(purchaseOrderService, purchaseOrderMapper);
                break;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
    }

    // ==================== submitOrder 测试 ====================
    @Test
    void testSubmitOrder_Success() {
        PurchaseOrderSubmitDTO dto = new PurchaseOrderSubmitDTO();
        dto.setSupplierId(1L);
        dto.setWarehouseId(2L);
        dto.setRemark("测试采购单");
        List<PurchaseOrderSubmitDTO.PurchaseDetailDTO> details = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            PurchaseOrderSubmitDTO.PurchaseDetailDTO detail = new PurchaseOrderSubmitDTO.PurchaseDetailDTO();
            detail.setMaterialId((long) i);
            detail.setPlanQty(10 * i);
            detail.setPrice(new BigDecimal("5.00"));
            details.add(detail);
        }
        dto.setDetails(details);

        try (MockedStatic<BaseContext> mocked = mockStatic(BaseContext.class)) {
            mocked.when(BaseContext::getCurrentId).thenReturn(1L);
            purchaseOrderService.submitOrder(dto);
            verify(purchaseDetailService).saveBatch(anyList());
            verify(operationLogService).saveLog(
                    eq("PURCHASE_SUBMIT"), anyString(), eq("purchase_order"), any());
        }
    }

    @Test
    void testSubmitOrder_EmptyDetails_ShouldThrow() {
        PurchaseOrderSubmitDTO dto = new PurchaseOrderSubmitDTO();
        dto.setSupplierId(1L);
        dto.setWarehouseId(2L);
        dto.setDetails(new ArrayList<>());
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> purchaseOrderService.submitOrder(dto));
        assertEquals("采购明细不能为空", ex.getMessage());
    }

    // ==================== rejectOrder 测试 ====================
    @Test
    void testRejectOrder_Success() {
        ScmPurchaseOrder order = new ScmPurchaseOrder();
        order.setId(1L);
        order.setOrderNo("P001");
        order.setStatus(0);
        order.setRemark("原始备注");

        // 现在 baseMapper 已注入，getById 可以正常工作
        when(purchaseOrderMapper.selectById(1L)).thenReturn(order);

        PurchaseOrderRejectDTO dto = new PurchaseOrderRejectDTO();
        dto.setId(1L);
        dto.setRejectReason("价格不合理");
        purchaseOrderService.rejectOrder(dto);

        assertEquals(9, order.getStatus());
        assertTrue(order.getRemark().contains("价格不合理"));
    }

    // ==================== confirmPayment 测试 ====================
    @Test
    void testConfirmPayment_Success() {
        ScmPurchaseOrder order = new ScmPurchaseOrder();
        order.setId(1L);
        order.setPaymentStatus(0);
        when(purchaseOrderMapper.selectById(1L)).thenReturn(order);

        purchaseOrderService.confirmPayment(1L);
        assertEquals(1, order.getPaymentStatus());
    }

    // ==================== countPending 测试 ====================
    @Test
    void testCountPending() {
        when(purchaseOrderMapper.countPending()).thenReturn(5L);
        assertEquals(5L, purchaseOrderService.countPending());
    }
}
