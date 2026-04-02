package com.student.scm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.student.scm.dto.MaterialPageQueryDTO;
import com.student.scm.entity.ScmMaterial;
import com.student.scm.mapper.ScmMaterialMapper;
import com.student.scm.service.IScmMaterialService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ScmMaterialServiceImpl extends ServiceImpl<ScmMaterialMapper, ScmMaterial> implements IScmMaterialService {

    @Override
    public Page<ScmMaterial> queryPageByCondition(MaterialPageQueryDTO queryDTO) {
        Page<ScmMaterial> pageInfo = new Page<>(queryDTO.getPage(), queryDTO.getPageSize());
        LambdaQueryWrapper<ScmMaterial> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.like(StringUtils.hasText(queryDTO.getName()), ScmMaterial::getName, queryDTO.getName());
        queryWrapper.orderByDesc(ScmMaterial::getCreateTime);

        this.page(pageInfo, queryWrapper);
        return pageInfo;
    }
}
