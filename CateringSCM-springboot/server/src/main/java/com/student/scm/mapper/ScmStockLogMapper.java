package com.student.scm.mapper;

import com.student.scm.entity.ScmStockLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface ScmStockLogMapper extends BaseMapper<ScmStockLog> {
    // 本周每日出入库统计
    @Select("SELECT DATE_FORMAT(create_time,'%w') AS dayOfWeek, " +
            "SUM(CASE WHEN type=1 THEN change_qty ELSE 0 END) AS inbound, " +
            "SUM(CASE WHEN type=2 THEN -change_qty ELSE 0 END) AS outbound " +
            "FROM scm_stock_log " +
            "WHERE YEARWEEK(create_time) = YEARWEEK(NOW()) " +
            "GROUP BY DATE_FORMAT(create_time,'%w') " +
            "ORDER BY dayOfWeek ASC")
    List<Map<String, Object>> getWeeklyStockMovement();
}
