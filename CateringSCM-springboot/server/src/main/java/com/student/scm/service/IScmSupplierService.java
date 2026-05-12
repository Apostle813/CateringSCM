package com.student.scm.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.student.scm.dto.SupplierPageQueryDTO;
import com.student.scm.entity.ScmSupplier;

public interface IScmSupplierService extends IService<ScmSupplier> {
    Page<ScmSupplier> queryPageByCondition(SupplierPageQueryDTO queryDTO);
    Long countActive();
}
