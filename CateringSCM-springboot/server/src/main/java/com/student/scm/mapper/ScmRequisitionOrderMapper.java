package com.student.scm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.student.scm.entity.ScmRequisitionOrder;
import org.apache.ibatis.annotations.Select;

public interface ScmRequisitionOrderMapper extends BaseMapper<ScmRequisitionOrder> {
    @Select("SELECT COUNT(*) FROM scm_requisition_order WHERE status = 0")
    Long countPending();
}
