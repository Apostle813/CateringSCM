package com.student.scm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.student.scm.dto.SupplierPageQueryDTO;
import com.student.scm.entity.ScmSupplier;
import com.student.scm.mapper.ScmSupplierMapper;
import com.student.scm.service.IScmSupplierService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ScmSupplierServiceImpl extends ServiceImpl<ScmSupplierMapper, ScmSupplier> implements IScmSupplierService {
    
    @Override
    public Page<ScmSupplier> queryPageByCondition(SupplierPageQueryDTO queryDTO) {
        Page<ScmSupplier> pageInfo = new Page<>(queryDTO.getPage(), queryDTO.getPageSize());
        
        LambdaQueryWrapper<ScmSupplier> queryWrapper = new LambdaQueryWrapper<>();
        
        // 按名称模糊查询
        if (StringUtils.hasText(queryDTO.getName())) {
            queryWrapper.like(ScmSupplier::getName, queryDTO.getName());
        }
        // 按联系人模糊查询
        if (StringUtils.hasText(queryDTO.getContact())) {
            queryWrapper.like(ScmSupplier::getContact, queryDTO.getContact());
        }
        // 按状态精准查询
        if (queryDTO.getStatus() != null) {
            queryWrapper.eq(ScmSupplier::getStatus, queryDTO.getStatus());
        }
        
        // 按ID升序排列
        queryWrapper.orderByAsc(ScmSupplier::getId);
        
        // 执行分页查询
        this.page(pageInfo, queryWrapper);
        
        return pageInfo;
    }

    @Override
    public Long countActive() {
        return this.baseMapper.countActive();
    }
}
