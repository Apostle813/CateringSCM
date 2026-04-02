package com.student.scm.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.student.scm.dto.MaterialPageQueryDTO;
import com.student.scm.entity.ScmMaterial;

public interface IScmMaterialService extends IService<ScmMaterial> {
    Page<ScmMaterial> queryPageByCondition(MaterialPageQueryDTO queryDTO);
}
