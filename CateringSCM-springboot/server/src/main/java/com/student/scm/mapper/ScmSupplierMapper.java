package com.student.scm.mapper;

import com.student.scm.entity.ScmSupplier;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;


public interface ScmSupplierMapper extends BaseMapper<ScmSupplier> {
    @Select("SELECT COUNT(*) FROM scm_supplier WHERE status = 1")
    Long countActive();
}
