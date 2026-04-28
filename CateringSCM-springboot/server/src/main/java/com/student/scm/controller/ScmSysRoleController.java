package com.student.scm.controller;

import com.student.scm.entity.ScmSysRole;
import com.student.scm.mapper.ScmSysRoleMapper;
import com.student.scm.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/role")
public class ScmSysRoleController {
    
    private final ScmSysRoleMapper scmSysRoleMapper;

    public ScmSysRoleController(ScmSysRoleMapper scmSysRoleMapper) {
        this.scmSysRoleMapper = scmSysRoleMapper;
    }

    @GetMapping("/list")
    public Result<List<ScmSysRole>> list() {
        return Result.success(scmSysRoleMapper.selectList(null));
    }
}
