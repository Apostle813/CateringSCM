package com.student.server.service;

import com.student.scm.context.BaseContext;
import com.student.scm.entity.ScmPurchaseOrder;
import com.student.scm.mapper.ScmPurchaseDetailMapper;
import com.student.scm.mapper.ScmPurchaseOrderMapper;
import com.student.scm.mapper.ScmInventoryMapper;
import com.student.scm.mapper.ScmStockLogMapper;
import com.student.scm.service.*;
import com.student.scm.service.impl.ScmPurchaseOrderServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScmPurchaseOrderServiceMoreTest {

    @Mock private IScmPurchaseDetailService purchaseDetailService;
    @Mock private IScmInventoryService inventoryService;
    @Mock private IScmStockLogService stockLogService;
    @Mock private IScmOperationLogService operationLogService;
    @Mock private ScmInventoryMapper inventoryMapper;
    @Mock private ScmPurchaseDetailMapper detailMapper;
    @Mock private ScmStockLogMapper stockLogMapper;
    @Mock private ScmPurchaseOrderMapper purchaseOrderMapper;

    // ===== T2-10: 重复入库拒绝 =====
    @Test
    void testExecuteInbound_AlreadyInbounded_ShouldThrow() {
        ScmPurchaseOrderServiceImpl service = buildService();
        ScmPurchaseOrder order = new ScmPurchaseOrder();
        order.setId(1L); order.setStatus(2); // 已入库
        when(purchaseOrderMapper.selectById(1L)).thenReturn(order);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.executeInbound(1L));
        assertTrue(ex.getMessage().contains("已入库"));
    }

    // ===== T2-12: 入库事务异常 =====
    @Test
    void testExecuteInbound_DetailEmpty_ShouldThrow() {
        ScmPurchaseOrderServiceImpl service = buildService();
        ScmPurchaseOrder order = new ScmPurchaseOrder();
        order.setId(1L); order.setStatus(1); // 待入库
        when(purchaseOrderMapper.selectById(1L)).thenReturn(order);
        when(detailMapper.selectList(any())).thenReturn(new java.util.ArrayList<>());

        assertThrows(RuntimeException.class, () -> service.executeInbound(1L));
        verify(purchaseOrderMapper, never()).updateById(any(ScmPurchaseOrder.class));
    }

    // ===== 审批通过 =====
    @Test
    void testAuditPass_Success() {
        ScmPurchaseOrderServiceImpl service = buildService();
        ScmPurchaseOrder order = new ScmPurchaseOrder();
        order.setId(1L); order.setStatus(0); // 待审核
        when(purchaseOrderMapper.selectById(1L)).thenReturn(order);

        try (MockedStatic<BaseContext> mocked = mockStatic(BaseContext.class)) {
            mocked.when(BaseContext::getCurrentId).thenReturn(1L);
            service.auditPass(1L);
            assertEquals(1, (int) order.getStatus());
        }
    }

    // ===== 审核已驳回的订单 =====
    @Test
    void testAuditPass_AlreadyRejected_ShouldThrow() {
        ScmPurchaseOrderServiceImpl service = buildService();
        ScmPurchaseOrder order = new ScmPurchaseOrder();
        order.setId(1L); order.setStatus(9); // 已驳回
        when(purchaseOrderMapper.selectById(1L)).thenReturn(order);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.auditPass(1L));
        assertTrue(ex.getMessage().contains("驳回") || ex.getMessage().contains("状态"));
    }

    private ScmPurchaseOrderServiceImpl buildService() {
        ScmPurchaseOrderServiceImpl s = new ScmPurchaseOrderServiceImpl(
                purchaseDetailService, inventoryService, stockLogService,
                operationLogService, inventoryMapper, detailMapper, stockLogMapper);
        try {
            Class<?> c = s.getClass();
            while (c != null) {
                try {
                    java.lang.reflect.Field f = c.getDeclaredField("baseMapper");
                    f.setAccessible(true); f.set(s, purchaseOrderMapper); break;
                } catch (NoSuchFieldException e) { c = c.getSuperclass(); }
            }
        } catch (Exception ignored) {}
        return s;
    }
}
