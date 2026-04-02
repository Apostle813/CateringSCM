package com.student.scm.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.student.scm.dto.StockLogPageQueryDTO;
import com.student.scm.entity.ScmStockLog;

public interface IScmStockLogService extends IService<ScmStockLog> {
    Page<ScmStockLog> queryPageByCondition(StockLogPageQueryDTO queryDTO);
}
