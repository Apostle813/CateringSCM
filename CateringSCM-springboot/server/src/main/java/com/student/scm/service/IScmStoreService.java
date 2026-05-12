package com.student.scm.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.student.scm.dto.StorePageQueryDTO;
import com.student.scm.entity.ScmStore;

public interface IScmStoreService extends IService<ScmStore> {
    Page<ScmStore> queryPageByCondition(StorePageQueryDTO queryDTO);
}
