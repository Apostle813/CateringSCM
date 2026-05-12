package com.student.scm.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.student.scm.dto.OperationLogPageQueryDTO;
import com.student.scm.entity.ScmOperationLog;

public interface IScmOperationLogService extends IService<ScmOperationLog> {
    /**
     * 写入操作日志（自动从 BaseContext 获取当前用户ID和姓名）
     */
    void saveLog(String operationType, String operationDesc, String targetType, Long targetId);

    /**
     * 写入操作日志（手动指定操作人，适用于登录等前置场景）
     */
    void saveLog(Long operatorId, String operatorName, String operationType, String operationDesc, String targetType, Long targetId);

    /**
     * 分页条件查询
     */
    Page<ScmOperationLog> queryPageByCondition(OperationLogPageQueryDTO queryDTO);
}
