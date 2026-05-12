package com.student.scm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.student.scm.dto.StorePageQueryDTO;
import com.student.scm.entity.ScmStore;
import com.student.scm.mapper.ScmStoreMapper;
import com.student.scm.service.IScmStoreService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ScmStoreServiceImpl extends ServiceImpl<ScmStoreMapper, ScmStore> implements IScmStoreService {

    @Override
    public Page<ScmStore> queryPageByCondition(StorePageQueryDTO queryDTO) {
        Page<ScmStore> pageInfo = new Page<>(queryDTO.getPage(), queryDTO.getPageSize());

        LambdaQueryWrapper<ScmStore> queryWrapper = new LambdaQueryWrapper<>();

        // 按门店名称模糊查询
        if (StringUtils.hasText(queryDTO.getName())) {
            queryWrapper.like(ScmStore::getName, queryDTO.getName());
        }
        // 按状态精准查询
        if (queryDTO.getStatus() != null) {
            queryWrapper.eq(ScmStore::getStatus, queryDTO.getStatus());
        }

        queryWrapper.orderByDesc(ScmStore::getCreateTime);

        this.page(pageInfo, queryWrapper);
        return pageInfo;
    }
}
