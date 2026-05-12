package com.student.scm.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.student.scm.dto.PurchaseOrderPageQueryDTO;
import com.student.scm.dto.PurchaseOrderRejectDTO;
import com.student.scm.dto.PurchaseOrderSubmitDTO;
import com.student.scm.entity.ScmPurchaseOrder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface IScmPurchaseOrderService extends IService<ScmPurchaseOrder> {

    void submitOrder(PurchaseOrderSubmitDTO dto);

    Page<ScmPurchaseOrder> queryPageByCondition(PurchaseOrderPageQueryDTO queryDTO);

    void auditPass(Long orderId);

    BigDecimal getMonthPurchaseAmount();

    void rejectOrder(PurchaseOrderRejectDTO dto);

    void confirmPayment(Long orderId);

    void executeInbound(Long id);

    Long countPending();

    List<Map<String, Object>> getMonthlyPurchaseTrend();
}
