package com.student.server.service;

import com.student.scm.context.BaseContext;
import com.student.scm.dto.RequisitionOrderSubmitDTO;
import com.student.scm.entity.ScmRequisitionOrder;
import com.student.scm.mapper.ScmRequisitionOrderMapper;
import com.student.scm.service.*;
import com.student.scm.service.impl.ScmRequisitionOrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScmRequisitionOrderServiceTest {

    @Mock private IScmRequisitionDetailService detailService;
    @Mock private IScmInventoryService inventoryService;
    @Mock private IScmStockLogService stockLogService;
    @Mock private IScmOperationLogService operationLogService;
    @Mock private ScmRequisitionOrderMapper requisitionOrderMapper;

    private ScmRequisitionOrderServiceImpl requisitionOrderService;

    @BeforeEach
    void setUp() throws Exception {
        requisitionOrderService = new ScmRequisitionOrderServiceImpl(
                detailService, inventoryService, stockLogService, operationLogService
        );
        Class<?> clazz = requisitionOrderService.getClass();
        while (clazz != null) {
            try {
                java.lang.reflect.Field f = clazz.getDeclaredField("baseMapper");
                f.setAccessible(true);
                f.set(requisitionOrderService, requisitionOrderMapper);
                break;
            } catch (NoSuchFieldException e) { clazz = clazz.getSuperclass(); }
        }
    }

    @Test
    void testSubmitOrder_Success() {
        RequisitionOrderSubmitDTO dto = new RequisitionOrderSubmitDTO();
        dto.setStoreId(1L);
        dto.setWarehouseId(2L);
        List<RequisitionOrderSubmitDTO.RequisitionDetailDTO> details = new ArrayList<>();
        for (int i = 1; i <= 2; i++) {
            RequisitionOrderSubmitDTO.RequisitionDetailDTO detail =
                    new RequisitionOrderSubmitDTO.RequisitionDetailDTO();
            detail.setMaterialId((long) i);
            detail.setPlanQty(20 * i);
            details.add(detail);
        }
        dto.setDetails(details);

        try (MockedStatic<BaseContext> mocked = mockStatic(BaseContext.class)) {
            mocked.when(BaseContext::getCurrentId).thenReturn(1L);
            requisitionOrderService.submitOrder(dto);
            verify(detailService, times(2)).save(any());
            verify(operationLogService).saveLog(
                    eq("REQUISITION_SUBMIT"), anyString(), eq("requisition_order"), any());
        }
    }

    @Test
    void testSubmitOrder_EmptyDetails_ShouldThrow() {
        RequisitionOrderSubmitDTO dto = new RequisitionOrderSubmitDTO();
        dto.setStoreId(1L);
        dto.setWarehouseId(2L);
        dto.setDetails(new ArrayList<>());
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> requisitionOrderService.submitOrder(dto));
        assertEquals("请购明细不能为空！", ex.getMessage());
    }

    @Test
    void testRejectOutbound_Success() {
        ScmRequisitionOrder order = new ScmRequisitionOrder();
        order.setId(1L);
        order.setOrderNo("OUT001");
        order.setStatus(0);
        when(requisitionOrderMapper.selectById(1L)).thenReturn(order);

        requisitionOrderService.rejectOutbound(1L);
        assertEquals(9, order.getStatus());
    }

    @Test
    void testConfirmPayment_Success() {
        ScmRequisitionOrder order = new ScmRequisitionOrder();
        order.setId(1L);
        order.setPaymentStatus(0);
        when(requisitionOrderMapper.selectById(1L)).thenReturn(order);

        requisitionOrderService.confirmPayment(1L);
        assertEquals(1, order.getPaymentStatus());
    }

    @Test
    void testConfirmPayment_AlreadyPaid_ShouldThrow() {
        ScmRequisitionOrder order = new ScmRequisitionOrder();
        order.setId(1L);
        order.setPaymentStatus(1);
        when(requisitionOrderMapper.selectById(1L)).thenReturn(order);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> requisitionOrderService.confirmPayment(1L));
        assertTrue(ex.getMessage().contains("已经结算过"));
    }

    @Test
    void testCountPending() {
        when(requisitionOrderMapper.countPending()).thenReturn(3L);
        assertEquals(3L, requisitionOrderService.countPending());
    }
}
