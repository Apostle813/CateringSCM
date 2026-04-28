package com.student.scm.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.student.scm.dto.RequisitionOrderPageQueryDTO;
import com.student.scm.dto.RequisitionOrderSubmitDTO;
import com.student.scm.entity.ScmRequisitionOrder;

public interface IScmRequisitionOrderService extends IService<ScmRequisitionOrder> {
    void submitOrder(RequisitionOrderSubmitDTO dto);
    Page<ScmRequisitionOrder> queryPageByCondition(RequisitionOrderPageQueryDTO queryDTO);
    void auditOutbound(Long orderId);
    void rejectOutbound(Long orderId);
    void confirmPayment(Long orderId);
}
