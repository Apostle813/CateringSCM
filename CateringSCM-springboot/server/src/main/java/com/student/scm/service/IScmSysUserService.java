package com.student.scm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.student.scm.dto.ChangePasswordDTO;
import com.student.scm.dto.ScmSysUserLoginDTO;
import com.student.scm.entity.ScmSysUser;
import com.student.scm.vo.ScmSysUserVO;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.scm.dto.ScmSysUserPageQueryDTO;

public interface IScmSysUserService extends IService<ScmSysUser> {

    ScmSysUserVO login(ScmSysUserLoginDTO loginUser);
    Page<ScmSysUser> queryPageByCondition(ScmSysUserPageQueryDTO queryDTO);
    ScmSysUserVO getProfile();  // 获取当前登录用户的个人信息
    void changePassword(ChangePasswordDTO dto);  // 修改密码

}
