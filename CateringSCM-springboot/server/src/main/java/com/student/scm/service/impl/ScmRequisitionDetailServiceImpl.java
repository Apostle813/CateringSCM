package com.student.scm.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.student.scm.entity.ScmRequisitionDetail;
import com.student.scm.mapper.ScmRequisitionDetailMapper;
import com.student.scm.service.IScmRequisitionDetailService;
import org.springframework.stereotype.Service;

@Service
public class ScmRequisitionDetailServiceImpl extends ServiceImpl<ScmRequisitionDetailMapper, ScmRequisitionDetail> implements IScmRequisitionDetailService {
}
