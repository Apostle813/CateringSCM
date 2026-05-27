package com.student.server.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.student.scm.context.BaseContext;
import com.student.scm.dto.InventoryAdjustDTO;
import com.student.scm.entity.ScmInventory;
import com.student.scm.entity.ScmMaterial;
import com.student.scm.entity.ScmStockLog;
import com.student.scm.entity.ScmWarehouse;
import com.student.scm.mapper.ScmInventoryMapper;
import com.student.scm.mapper.ScmMaterialMapper;
import com.student.scm.mapper.ScmWarehouseMapper;
import com.student.scm.service.IScmOperationLogService;
import com.student.scm.service.IScmStockLogService;
import com.student.scm.service.impl.ScmInventoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScmInventoryServiceTest {

    @Mock private IScmStockLogService stockLogService;
    @Mock private IScmOperationLogService operationLogService;
    @Mock private ScmWarehouseMapper warehouseMapper;
    @Mock private ScmMaterialMapper materialMapper;
    @Mock private ScmInventoryMapper inventoryMapper;

    private ScmInventoryServiceImpl inventoryService;

    @BeforeEach
    void setUp() throws Exception {
        inventoryService = new ScmInventoryServiceImpl(
                stockLogService, operationLogService, warehouseMapper, materialMapper);
        Class<?> clazz = inventoryService.getClass();
        while (clazz != null) {
            try {
                java.lang.reflect.Field f = clazz.getDeclaredField("baseMapper");
                f.setAccessible(true);
                f.set(inventoryService, inventoryMapper);
                break;
            } catch (NoSuchFieldException e) { clazz = clazz.getSuperclass(); }
        }
    }

    // ===== 库存查询：有记录 =====
    @Test
    void testGetStockQty_HasRecord() {
        ScmInventory inv = new ScmInventory();
        inv.setId(1L); inv.setWarehouseId(1L); inv.setMaterialId(1L); inv.setQuantity(80);
        when(inventoryMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(inv);

        Integer qty = inventoryService.getStockQty(1L, 1L);
        assertEquals(80, qty);
    }

    // ===== 库存查询：无记录返回0 =====
    @Test
    void testGetStockQty_NoRecord_ReturnsNull() {
        when(inventoryMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        assertNull(inventoryService.getStockQty(1L, 99L));
    }

    // ===== 盘点调整成功 =====
    @Test
    void testAdjust_Success() {
        InventoryAdjustDTO dto = new InventoryAdjustDTO();
        dto.setWarehouseId(1L); dto.setMaterialId(1L); dto.setRealQty(100);

        ScmInventory inv = new ScmInventory();
        inv.setId(1L); inv.setWarehouseId(1L); inv.setMaterialId(1L); inv.setQuantity(50);

        ScmWarehouse wh = new ScmWarehouse();
        wh.setId(1L); wh.setName("主仓库");
        ScmMaterial mat = new ScmMaterial();
        mat.setId(1L); mat.setName("大米"); mat.setPrice(new BigDecimal("5.00"));

        when(inventoryMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(inv);
        when(warehouseMapper.selectById(1L)).thenReturn(wh);
        when(materialMapper.selectById(1L)).thenReturn(mat);

        try (MockedStatic<BaseContext> mocked = mockStatic(BaseContext.class)) {
            mocked.when(BaseContext::getCurrentId).thenReturn(1L);
            inventoryService.adjust(dto);
            assertEquals(100, (int) inv.getQuantity());
            verify(stockLogService).save(any(ScmStockLog.class));
        }
    }

    // ===== 盘点调整为相同值，直接返回 =====
    @Test
    void testAdjust_SameQty_NoOp() {
        InventoryAdjustDTO dto = new InventoryAdjustDTO();
        dto.setWarehouseId(1L); dto.setMaterialId(1L); dto.setRealQty(50);

        ScmInventory inv = new ScmInventory();
        inv.setId(1L); inv.setWarehouseId(1L); inv.setMaterialId(1L); inv.setQuantity(50);

        when(inventoryMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(inv);

        inventoryService.adjust(dto);
        verify(stockLogService, never()).save(any());
    }

    // ===== 出库：库存不足抛异常 =====
    @Test
    void testOutbound_InsufficientStock_ShouldThrow() {
        ScmInventory inv = new ScmInventory();
        inv.setId(1L); inv.setWarehouseId(1L); inv.setMaterialId(1L); inv.setQuantity(5);
        when(inventoryMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(inv);

        com.student.scm.dto.InventoryOutboundDTO dto =
                new com.student.scm.dto.InventoryOutboundDTO();
        dto.setWarehouseId(1L); dto.setMaterialId(1L); dto.setOutQty(100);
        dto.setReferenceNo("OUT001");

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> inventoryService.outbound(dto));
        assertTrue(ex.getMessage().contains("不足"));
    }

    // ===== 盘点食材不存在 =====
    @Test
    void testAdjust_MaterialNotFound_ShouldThrow() {
        InventoryAdjustDTO dto = new InventoryAdjustDTO();
        dto.setWarehouseId(1L); dto.setMaterialId(99L); dto.setRealQty(50);

        when(inventoryMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> inventoryService.adjust(dto));
        assertTrue(ex.getMessage().contains("不存在"));
    }
}
