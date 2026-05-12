package com.student.scm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.student.scm.context.BaseContext;
import com.student.scm.dto.OperationLogPageQueryDTO;
import com.student.scm.entity.ScmOperationLog;
import com.student.scm.entity.ScmSysUser;
import com.student.scm.mapper.ScmOperationLogMapper;
import com.student.scm.mapper.ScmSysUserMapper;
import com.student.scm.service.IScmOperationLogService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class ScmOperationLogServiceImpl extends ServiceImpl<ScmOperationLogMapper, ScmOperationLog> implements IScmOperationLogService {

    private final ScmSysUserMapper sysUserMapper;

    public ScmOperationLogServiceImpl(ScmSysUserMapper sysUserMapper) {
        this.sysUserMapper = sysUserMapper;
    }

    @Override
    public void saveLog(String operationType, String operationDesc, String targetType, Long targetId) {
        Long operatorId = BaseContext.getCurrentId();
        String operatorName = null;
        if (operatorId != null) {
            ScmSysUser user = sysUserMapper.selectById(operatorId);
            if (user != null) {
                operatorName = user.getRealName();
            }
        }
        doSave(operatorId, operatorName, operationType, operationDesc, targetType, targetId);
    }

    @Override
    public void saveLog(Long operatorId, String operatorName, String operationType, String operationDesc, String targetType, Long targetId) {
        doSave(operatorId, operatorName, operationType, operationDesc, targetType, targetId);
    }

    private void doSave(Long operatorId, String operatorName, String operationType, String operationDesc, String targetType, Long targetId) {
        ScmOperationLog log = new ScmOperationLog();
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName);
        log.setOperationType(operationType);
        log.setOperationDesc(operationDesc);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setCreateTime(LocalDateTime.now());
        this.save(log);
    }

    @Override
    public Page<ScmOperationLog> queryPageByCondition(OperationLogPageQueryDTO queryDTO) {
        Page<ScmOperationLog> page = new Page<>(queryDTO.getPage(), queryDTO.getPageSize());

        LambdaQueryWrapper<ScmOperationLog> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(queryDTO.getOperationType())) {
            wrapper.eq(ScmOperationLog::getOperationType, queryDTO.getOperationType());
        }
        if (StringUtils.hasText(queryDTO.getOperatorName())) {
            wrapper.like(ScmOperationLog::getOperatorName, queryDTO.getOperatorName());
        }
        if (StringUtils.hasText(queryDTO.getTargetType())) {
            wrapper.eq(ScmOperationLog::getTargetType, queryDTO.getTargetType());
        }
        if (StringUtils.hasText(queryDTO.getStartDate())) {
            wrapper.ge(ScmOperationLog::getCreateTime, queryDTO.getStartDate() + " 00:00:00");
        }
        if (StringUtils.hasText(queryDTO.getEndDate())) {
            wrapper.le(ScmOperationLog::getCreateTime, queryDTO.getEndDate() + " 23:59:59");
        }

        wrapper.orderByDesc(ScmOperationLog::getCreateTime);
        return this.page(page, wrapper);
    }
}
