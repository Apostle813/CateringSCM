package com.student.scm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.student.scm.dto.StockLogPageQueryDTO;
import com.student.scm.entity.ScmStockLog;
import com.student.scm.mapper.ScmStockLogMapper;
import com.student.scm.service.IScmStockLogService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ScmStockLogServiceImpl extends ServiceImpl<ScmStockLogMapper, ScmStockLog> implements IScmStockLogService {

    @Override
    public Page<ScmStockLog> queryPageByCondition(StockLogPageQueryDTO queryDTO) {
        Page<ScmStockLog> pageInfo = new Page<>(queryDTO.getPage(), queryDTO.getPageSize());
        LambdaQueryWrapper<ScmStockLog> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(StringUtils.hasText(queryDTO.getReferenceNo()), ScmStockLog::getReferenceNo, queryDTO.getReferenceNo());
        queryWrapper.orderByDesc(ScmStockLog::getCreateTime); // 流水必须按时间倒序

        this.page(pageInfo, queryWrapper);
        return pageInfo;
    }
}
