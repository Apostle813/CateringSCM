package com.student.scm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.student.scm.entity.ScmInventory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface ScmInventoryMapper extends BaseMapper<ScmInventory> {
    // 统计总资产: SUM(数量 * 单价)
    BigDecimal sumTotalInventoryAsset();

    // 查询库存低于 20 的告警列表: 分组并联查名字
    List<Map<String, Object>> getLowStockAlerts();

    // 各仓库资产分布
    List<Map<String, Object>> getWarehouseAssetDistribution();

    // 本月入库总数
    @Select("SELECT IFNULL(SUM(change_qty), 0) FROM scm_stock_log WHERE type = 1 AND DATE_FORMAT(create_time,'%Y-%m') = DATE_FORMAT(NOW(), '%Y-%m')")
    Integer getMonthInboundQty();

    // 本月出库总数 (出库change_qty为负数，取反得正数)
    @Select("SELECT IFNULL(SUM(-change_qty), 0) FROM scm_stock_log WHERE type = 2 AND DATE_FORMAT(create_time,'%Y-%m') = DATE_FORMAT(NOW(), '%Y-%m')")
    Integer getMonthOutboundQty();
}
