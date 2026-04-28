package com.student.scm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.student.scm.dto.ScmSysUserLoginDTO;
import com.student.scm.entity.ScmSysUser;
import com.student.scm.vo.ScmSysUserVO;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.scm.dto.ScmSysUserPageQueryDTO;

public interface IScmSysUserService extends IService<ScmSysUser> {

    ScmSysUserVO login(ScmSysUserLoginDTO loginUser);
    Page<ScmSysUser> queryPageByCondition(ScmSysUserPageQueryDTO queryDTO);

}
