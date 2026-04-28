package com.student.scm.controller;


import com.student.scm.dto.ScmSysUserLoginDTO;
import com.student.scm.entity.ScmSysUser;
import com.student.scm.properties.JwtProperties;
import com.student.scm.result.Result;
import com.student.scm.service.IScmSysUserService;
import com.student.scm.vo.ScmSysUserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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
        // 默认密码设置为 123456，并进行 MD5 加密
        scmSysUser.setPassword(org.springframework.util.DigestUtils.md5DigestAsHex("123456".getBytes()));
        scmsysuserservice.save(scmSysUser);
        return Result.success("员工账号创建成功");
    }

    @GetMapping("/page")
    public Result<com.baomidou.mybatisplus.extension.plugins.pagination.Page<ScmSysUser>> page(com.student.scm.dto.ScmSysUserPageQueryDTO queryDTO) {
        return Result.success(scmsysuserservice.queryPageByCondition(queryDTO));
    }

    @PutMapping("/update")
    public Result<String> update(@RequestBody ScmSysUser scmSysUser) {
        scmsysuserservice.updateById(scmSysUser);
        return Result.success("修改成功");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@org.springframework.web.bind.annotation.PathVariable Long id) {
        scmsysuserservice.removeById(id);
        return Result.success("删除成功");
    }
}
