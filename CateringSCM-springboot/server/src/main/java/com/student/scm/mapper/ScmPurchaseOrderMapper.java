package com.student.scm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.student.scm.entity.ScmPurchaseOrder;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
@Mapper
public interface ScmPurchaseOrderMapper extends BaseMapper<ScmPurchaseOrder> {
    BigDecimal sumMonthPurchaseAmount();
}
