package com.student.scm.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.student.scm.dto.PurchaseOrderPageQueryDTO;
import com.student.scm.dto.PurchaseOrderRejectDTO;
import com.student.scm.dto.PurchaseOrderSubmitDTO;
import com.student.scm.entity.ScmPurchaseOrder;

import java.math.BigDecimal;

public interface IScmPurchaseOrderService extends IService<ScmPurchaseOrder> {
    /**
     * 提交采购申请单
     *
     * @param dto 前端传来的复合数据
     */
    void submitOrder(PurchaseOrderSubmitDTO dto);

    Page<ScmPurchaseOrder> queryPageByCondition(PurchaseOrderPageQueryDTO queryDTO);

    void auditInbound(Long orderId);

    BigDecimal getMonthPurchaseAmount();

    void rejectOrder(PurchaseOrderRejectDTO dto);
}
