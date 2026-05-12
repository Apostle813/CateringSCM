package com.student.scm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.student.scm.entity.ScmRequisitionDetail;
import com.student.scm.vo.ScmRequisitionDetailVO;

import java.util.List;

public interface IScmRequisitionDetailService extends IService<ScmRequisitionDetail> {
    List<ScmRequisitionDetailVO> listByOrderId(Long orderId);
}
