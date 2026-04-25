package com.student.scm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.student.scm.dto.StockLogPageQueryDTO;
import com.student.scm.entity.ScmStockLog;
import com.student.scm.result.PageResult;

public interface IScmStockLogService extends IService<ScmStockLog> {
    PageResult queryPageByCondition(StockLogPageQueryDTO queryDTO);
}
