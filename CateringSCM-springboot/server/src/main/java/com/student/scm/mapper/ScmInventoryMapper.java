package com.student.scm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.student.scm.entity.ScmInventory;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface ScmInventoryMapper extends BaseMapper<ScmInventory> {
    // 统计总资产: SUM(数量 * 单价)
    BigDecimal sumTotalInventoryAsset();

    // 查询库存低于 20 的告警列表: 分组并联查名字
    List<Map<String, Object>> getLowStockAlerts();
}
