package com.student.scm.service.impl;

import com.student.scm.entity.ScmPurchaseOrder;
import com.student.scm.mapper.ScmPurchaseOrderMapper;
import com.student.scm.service.IScmPurchaseOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class ScmPurchaseOrderServiceImpl extends ServiceImpl<ScmPurchaseOrderMapper, ScmPurchaseOrder> implements IScmPurchaseOrderService {

}
