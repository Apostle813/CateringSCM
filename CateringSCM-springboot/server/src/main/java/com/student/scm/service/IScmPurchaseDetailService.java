package com.student.scm.service;

import com.student.scm.entity.ScmPurchaseDetail;
import com.baomidou.mybatisplus.extension.service.IService;
import com.student.scm.vo.ScmPurchaseDetailVO;

import java.util.List;

public interface IScmPurchaseDetailService extends IService<ScmPurchaseDetail> {
    List<ScmPurchaseDetailVO> listByOrderId(Long orderId);
}
