package com.student.scm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.student.scm.entity.ScmPurchaseOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface ScmPurchaseOrderMapper extends BaseMapper<ScmPurchaseOrder> {
    BigDecimal sumMonthPurchaseAmount();

    @Select("SELECT COUNT(*) FROM scm_purchase_order WHERE status = 0")
    Long countPending();

    @Select("SELECT DATE_FORMAT(create_time,'%Y-%m') AS month, IFNULL(SUM(total_amount),0) AS amount " +
            "FROM scm_purchase_order WHERE status = 2 AND create_time >= DATE_SUB(NOW(), INTERVAL 6 MONTH) " +
            "GROUP BY DATE_FORMAT(create_time,'%Y-%m') ORDER BY month ASC")
    List<Map<String, Object>> getMonthlyPurchaseTrend();
}
