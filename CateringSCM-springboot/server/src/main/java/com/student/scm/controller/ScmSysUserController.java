package com.student.scm.controller;


import com.student.scm.dto.ScmSysUserLoginDTO;
import com.student.scm.entity.ScmSysUser;
import com.student.scm.properties.JwtProperties;
import com.student.scm.result.Result;
import com.student.scm.service.IScmSysUserService;
import com.student.scm.utils.JwtUtil;
import com.student.scm.vo.ScmSysUserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
@Slf4j
public class ScmSysUserController {
    private IScmSysUserService scmsysuserservice;
    private JwtProperties jwtProperties;

    public ScmSysUserController(IScmSysUserService scmsysuserservice, JwtProperties jwtProperties) {
        this.scmsysuserservice = scmsysuserservice;
        this.jwtProperties = jwtProperties;
    }

    @PostMapping("/login")
    public Result<ScmSysUserVO> login(@RequestBody ScmSysUserLoginDTO userLoginDTO) {
        try {
            log.info("用户传过来的登录信息DTO:{}", userLoginDTO);
            ScmSysUserVO user = scmsysuserservice.login(userLoginDTO);
            return Result.success(user);
        } catch (RuntimeException e) {
            // 如果 Service 里抛出了异常（比如密码错误），在这里捕获并返回错误状态
            return Result.error(e.getMessage());
        }
    }

    @PostMapping()
    public Result<String> save(@RequestBody ScmSysUser scmSysUser) {
        // 默认密码设置为 123456
        scmSysUser.setPassword("123456");
        scmsysuserservice.save(scmSysUser);
        return Result.success("员工账号创建成功");
    }
}
